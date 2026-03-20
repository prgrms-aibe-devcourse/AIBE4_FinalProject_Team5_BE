package kr.java.coditor.domain.grade.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

import kr.java.coditor.global.util.gemini.GeminiApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import kr.java.coditor.domain.grade.dto.CodeSubmissionRequest;
import kr.java.coditor.domain.grade.entity.Submit;
import kr.java.coditor.domain.grade.langset.LanguageStrategy;
import kr.java.coditor.domain.grade.langset.LanguageStrategyFactory;
import kr.java.coditor.domain.grade.repository.SubmitRepository;
import kr.java.coditor.domain.notification.dto.NotificationMessage;
import kr.java.coditor.domain.notification.service.RedisPublisher;
import kr.java.coditor.domain.problem.entity.Problem;
import kr.java.coditor.domain.problem.entity.TestCase;
import kr.java.coditor.domain.problem.repository.ProblemRepository;
import kr.java.coditor.domain.problem.repository.TestCaseRepository;
import kr.java.coditor.global.config.RabbitMqConfig;

@Service
public class ScoringConsumer {

	private static final Logger log = LoggerFactory.getLogger(ScoringConsumer.class);

	private final SubmitRepository submitRepository;
	private final RedisPublisher redisPublisher;
	private final LanguageStrategyFactory strategyFactory;
	private final ProblemRepository problemRepository;
	private final TestCaseRepository testCaseRepository;
	private final GeminiApiService geminiApiService;

	public ScoringConsumer(
		SubmitRepository submitRepository,
		RedisPublisher redisPublisher,
		LanguageStrategyFactory strategyFactory,
		ProblemRepository problemRepository,
		TestCaseRepository testCaseRepository,
		GeminiApiService geminiApiService
	) {
		this.submitRepository = submitRepository;
		this.redisPublisher = redisPublisher;
		this.strategyFactory = strategyFactory;
		this.problemRepository = problemRepository;
		this.testCaseRepository = testCaseRepository;
		this.geminiApiService = geminiApiService;
	}

	@RabbitListener(queues = RabbitMqConfig.SCORING_QUEUE)
	public void consumeScoringRequest(CodeSubmissionRequest request) {
		Problem problem = problemRepository.findById(request.getProblemId()).orElse(null);
		if (problem == null) {
			log.error("존재하지 않는 문제 번호: {}", request.getProblemId());
			return;
		}

		List<TestCase> testCases = testCaseRepository.findByProblem(problem);
		if (testCases.isEmpty()) {
			log.error("테스트케이스가 없습니다. problemId: {}", problem.getId());
			return;
		}

		LanguageStrategy strategy;
		try {
			strategy = strategyFactory.findStrategy(request.getLanguage());
		} catch (IllegalArgumentException e) {
			log.error("지원하지 않는 언어: {}", request.getLanguage(), e);
			return;
		}

		String executionId = UUID.randomUUID().toString();
		File dir = new File(System.getProperty("user.dir"), "temp_submissions/" + executionId);
		String hostDirPath = dir.getAbsolutePath();

		if (!dir.exists() && !dir.mkdirs()) {
			log.error("임시 디렉토리 생성 실패. path={}", hostDirPath);
			return;
		}

		try {
			File sourceFile = new File(dir, strategy.getFileName());
			try (FileWriter writer = new FileWriter(sourceFile)) {
				writer.write(request.getSourceCode());
			}

			// AI리뷰는 비동기처리
			CompletableFuture<String> aiReviewFuture = CompletableFuture
				.supplyAsync(() -> {
					try {
						return geminiApiService.getAiReview(
							problem.getTitle(),
							problem.getContent(),
							request.getSourceCode(),
							request.getLanguage(),
							request.getPersona()
						);
					} catch (Exception e) {
						log.error("AI 리뷰 백그라운드 처리 실패. executionId: {}", executionId, e);
						return "[시스템 알림] AI 리뷰 요청 중 오류가 발생했습니다.";
					}
				})
				.completeOnTimeout("[시스템 알림] AI 리뷰 생성이 지연되어 이번 제출에서는 생략되었습니다.", 30, TimeUnit.SECONDS);

			String finalResult = "CORRECT";
			String failReason = null;
			int maxRunningTime = 0;
			int maxMemoryUsage = 0; // 현재 미측정
			StringBuilder consoleLog = new StringBuilder();

			for (int i = 0; i < testCases.size(); i++) {
				TestCase tc = testCases.get(i);
				String containerName = "coditor-exec-" + executionId + "-" + i;

				File inputFile = new File(dir, "input.txt");
				File answerFile = new File(dir, "answer.txt");

				consoleLog.append(String.format("▶ 테스트케이스 %d ... ", i + 1));

				try {
					try (InputStream in = new URL(tc.getInputUrl()).openStream()) {
						Files.copy(in, inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					try (InputStream in = new URL(tc.getOutputUrl()).openStream()) {
						Files.copy(in, answerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}

					ProcessBuilder processBuilder = new ProcessBuilder(
						"docker", "run", "--rm",
						"--name", containerName,
						"--memory=" + problem.getMemoryLimit() + "m",
						"-v", hostDirPath + ":/app",
						"-w", "/app",
						strategy.getDockerImage(),
						"sh", "-c", strategy.getRunCommand()
					);

					long startTime = System.currentTimeMillis();
					Process process = processBuilder.start();

					CompletableFuture<String> outputFuture =
						CompletableFuture.supplyAsync(() -> readStream(process.getInputStream()));
					CompletableFuture<String> errorFuture =
						CompletableFuture.supplyAsync(() -> readStream(process.getErrorStream()));

					long timeoutMillis = (long) (problem.getTimeLimit() * 1000);
					boolean isFinished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
					long elapsed = System.currentTimeMillis() - startTime;
					maxRunningTime = Math.max(maxRunningTime, (int) elapsed);

					if (!isFinished) {
						process.destroyForcibly();
						cleanupZombieContainer(containerName);
						finalResult = "TIME_LIMIT_EXCEEDED";
						failReason = (i + 1) + "번 테스트케이스 시간 초과";
						consoleLog.append("시간 초과 X\n");
						break;
					}

					String output = outputFuture.join();
					String errorOutput = errorFuture.join();

					int exitCode = process.exitValue();
					if (exitCode == 137) {
						finalResult = "MEMORY_LIMIT_EXCEEDED";
						failReason = (i + 1) + "번 테스트케이스 메모리 초과";
						consoleLog.append("메모리 초과 X\n");
						break;
					} else if (exitCode != 0) {
						finalResult = "RUNTIME_ERROR";
						failReason = errorOutput.isBlank() ? "프로그램 실행 중 오류가 발생했습니다." : errorOutput.trim();
						consoleLog.append("런타임 에러 X\n").append(failReason).append("\n");
						break;
					}

					String expectedAnswer = Files.readString(answerFile.toPath()).trim();
					String actualAnswer = output.trim();

					if (!expectedAnswer.equals(actualAnswer)) {
						finalResult = "WRONG_ANSWER";
						failReason = (i + 1) + "번 테스트케이스 오답";
						consoleLog.append("오답 X\n");
						consoleLog.append("  [기대값]: ").append(expectedAnswer).append("\n");
						consoleLog.append("  [출력값]: ").append(actualAnswer).append("\n");
						break;
					}

					consoleLog.append("통과 O (").append(elapsed).append("ms)\n");
					consoleLog.append("  [출력]: ").append(actualAnswer.isEmpty() ? "(출력 내용 없음)" : actualAnswer).append("\n");

				} catch (Exception e) {
					log.error("채점 중 예외 발생. executionId: {}", executionId, e);
					finalResult = "SERVER_ERROR";
					failReason = "서버 내부 채점 오류";
					consoleLog.append("서버 에러 X\n");
					break;
				}
			}

			// 채점이 완료되면 AI 응답을 기다리지 않고 먼저 DB에 저장시킴
			Submit submit = new Submit(
				request.getMemberId(),
				request.getProblemId(),
				request.getLanguage(),
				request.getSourceCode(),
				finalResult,
				maxMemoryUsage,
				maxRunningTime,
				failReason
			);
			final Submit savedSubmit = submitRepository.save(submit);

			sendGradingNotification(request, finalResult, maxRunningTime, consoleLog.toString());
			log.info("채점 완료, 1차 알림 발송. executionId: {}, Result: {}", executionId, finalResult);

			// 백그라운드에서 AI 리뷰 생성이 완료되면 실행될 콜백 등록
			aiReviewFuture.thenAccept(aiReview -> {
				submitRepository.findById(savedSubmit.getId()).ifPresent(s -> {
					s.setAiReview(aiReview);
					submitRepository.save(s);
				});


				sendAiReviewNotification(request, aiReview);
				log.info("AI 리뷰 완료, 2차 알림 발송. executionId: {}", executionId);
			});

		} catch (IOException e) {
			log.error("소스코드 생성 오류", e);
		} finally {
			deleteDirectory(dir.toPath());
		}
	}

	private String readStream(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			log.error("프로세스 출력 읽기 실패", e);
		}
		return sb.toString();
	}

	// 채점 결과 전용 알림
	private void sendGradingNotification(CodeSubmissionRequest request, String finalResult, int maxRunningTime, String consoleLog) {
		StringBuilder messageText = new StringBuilder();
		messageText.append("[채점 결과]\n");
		messageText.append("문제 번호: ").append(request.getProblemId()).append("\n");
		messageText.append("결과: ").append(convertResultToKorean(finalResult)).append("\n");
		messageText.append("최대 실행 시간: ").append(maxRunningTime).append(" ms\n\n");
		messageText.append("[채점 상세 로그]\n");
		messageText.append(consoleLog);

		NotificationMessage notificationMessage = new NotificationMessage(
			request.getMemberId(),
			messageText.toString(),
			"/problems/" + request.getProblemId()
		);

		redisPublisher.publish(notificationMessage);
	}

	// AI 리뷰 전용 알림
	private void sendAiReviewNotification(CodeSubmissionRequest request, String aiReview) {
		StringBuilder messageText = new StringBuilder();
		messageText.append("[AI 코드 리뷰]\n\n");
		messageText.append(aiReview);

		NotificationMessage notificationMessage = new NotificationMessage(
			request.getMemberId(),
			messageText.toString(),
			"/problems/" + request.getProblemId()
		);

		redisPublisher.publish(notificationMessage);
	}

	private String convertResultToKorean(String result) {
		if ("CORRECT".equals(result)) return "정답";
		if ("WRONG_ANSWER".equals(result)) return "오답";
		if ("TIME_LIMIT_EXCEEDED".equals(result)) return "시간 초과";
		if ("MEMORY_LIMIT_EXCEEDED".equals(result)) return "메모리 초과";
		if ("RUNTIME_ERROR".equals(result)) return "런타임 에러";
		if ("SERVER_ERROR".equals(result)) return "서버 오류";
		return result;
	}

	private void cleanupZombieContainer(String containerName) {
		try {
			ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", containerName);
			pb.start().waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("컨테이너 삭제 중 인터럽트 발생: {}", containerName, e);
		} catch (IOException e) {
			log.error("컨테이너 삭제 실패: {}", containerName, e);
		}
	}

	private void deleteDirectory(Path dirPath) {
		try {
			if (!Files.exists(dirPath)) return;
			Files.walk(dirPath)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		} catch (IOException e) {
			log.warn("임시 디렉토리 삭제 실패: {}", dirPath, e);
		}
	}
}
