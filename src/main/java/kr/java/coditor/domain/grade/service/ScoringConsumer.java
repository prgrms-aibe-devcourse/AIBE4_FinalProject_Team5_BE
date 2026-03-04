package kr.java.coditor.domain.grade.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import kr.java.coditor.domain.grade.dto.CodeSubmissionRequest;
import kr.java.coditor.domain.grade.entity.Submit;
import kr.java.coditor.domain.grade.repository.SubmitRepository;
import kr.java.coditor.domain.grade.langset.LanguageStrategy;
import kr.java.coditor.domain.grade.langset.LanguageStrategyFactory;
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

	public ScoringConsumer(
		SubmitRepository submitRepository,
		RedisPublisher redisPublisher,
		LanguageStrategyFactory strategyFactory,
		ProblemRepository problemRepository,
		TestCaseRepository testCaseRepository
	) {
		this.submitRepository = submitRepository;
		this.redisPublisher = redisPublisher;
		this.strategyFactory = strategyFactory;
		this.problemRepository = problemRepository;
		this.testCaseRepository = testCaseRepository;
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

		//언어 감지 테스트 코드
		LanguageStrategy strategy;
		try {
			strategy = strategyFactory.findStrategy(request.getLanguage());
		} catch (IllegalArgumentException e) {
			log.error("지원하지 않는 언어: {}", request.getLanguage(), e);
			return;
		}

		String executionId = UUID.randomUUID().toString();

		// --- [수정된 부분] 윈도우 환경 대응을 위한 절대 경로 설정 ---
		// 현재 프로젝트 폴더 하위에 temp_submissions 폴더 생성
		File dir = new File(System.getProperty("user.dir"), "temp_submissions/" + executionId);
		String hostDirPath = dir.getAbsolutePath(); // C:\Users\... 형식의 절대 경로 추출

		if (!dir.exists()) {
			dir.mkdirs();
		}

		// 소스코드 파일 생성
		File sourceFile = new File(dir, strategy.getFileName());
		try (FileWriter writer = new FileWriter(sourceFile)) {
			writer.write(request.getSourceCode());
		} catch (IOException e) {
			log.error("소스코드 생성 오류", e);
			return;
		}

		String finalResult = "CORRECT";
		String failReason = null;
		int maxRunningTime = 0;
		int maxMemoryUsage = 0; // Docker Stats API 연동 전까지 임시로 0 처리

		// 테스트케이스 순회 검증
		for (int i = 0; i < testCases.size(); i++) {
			TestCase tc = testCases.get(i);
			String containerName = "coditor-exec-" + executionId + "-" + i;

			File inputFile = new File(dir, "input.txt");
			File answerFile = new File(dir, "answer.txt");

			try {
				// S3 (또는 URL)에서 입출력 파일 다운로드
				Files.copy(new URL(tc.getInputUrl()).openStream(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				Files.copy(new URL(tc.getOutputUrl()).openStream(), answerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				ProcessBuilder processBuilder = new ProcessBuilder(
					"docker", "run", "--rm",
					"--name", containerName,
					"--memory=" + problem.getMemoryLimit() + "m",
					"-v", hostDirPath + ":/app", // 추출한 절대 경로가 여기에 들어감
					"-w", "/app",
					strategy.getDockerImage(),
					"sh", "-c", strategy.getRunCommand()
				);

				long startTime = System.currentTimeMillis();
				Process process = processBuilder.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

				StringBuilder output = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}

				StringBuilder errorOutput = new StringBuilder();
				while ((line = errorReader.readLine()) != null) {
					errorOutput.append(line).append("\n");
				}

				// Double 타입의 초(Seconds)를 1000을 곱해 long 타입의 밀리초(Milliseconds)로 변환
				long timeoutMillis = (long) (problem.getTimeLimit() * 1000);
				boolean isFinished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
				long elapsed = System.currentTimeMillis() - startTime;
				maxRunningTime = Math.max(maxRunningTime, (int) elapsed);

				if (!isFinished) {
					process.destroyForcibly();
					cleanupZombieContainer(containerName);
					finalResult = "TIME_LIMIT_EXCEEDED";
					failReason = (i + 1) + "번 테스트케이스 시간 초과";
					break;
				}

				int exitCode = process.exitValue();
				if (exitCode == 137) {
					finalResult = "MEMORY_LIMIT_EXCEEDED";
					failReason = (i + 1) + "번 테스트케이스 메모리 초과";
					break;
				} else if (exitCode != 0) {
					finalResult = "RUNTIME_ERROR";
					failReason = errorOutput.toString();
					break;
				}

				// 정답 비교 (양쪽 끝 공백 제거 후 비교)
				String expectedAnswer = Files.readString(answerFile.toPath()).trim();
				String actualAnswer = output.toString().trim();

				if (!expectedAnswer.equals(actualAnswer)) {
					finalResult = "WRONG_ANSWER";
					failReason = (i + 1) + "번 테스트케이스 오답";
					break;
				}

			} catch (Exception e) {
				log.error("채점 중 예외 발생. executionId: {}", executionId, e);
				finalResult = "SERVER_ERROR";
				failReason = "서버 내부 채점 오류";
				break;
			}
		}

		// DB 저장 (submits 테이블)
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
		submitRepository.save(submit);

		// Redis 알림 전송
		String messageText = String.format("문제 번호 %d 채점 완료: %s", request.getProblemId(), finalResult);
		NotificationMessage notificationMessage = new NotificationMessage(request.getMemberId(), messageText);
		redisPublisher.publish(notificationMessage);

		log.info("채점 완료. executionId: {}, Result: {}", executionId, finalResult);
	}

	private void cleanupZombieContainer(String containerName) {
		try {
			ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", containerName);
			pb.start().waitFor();
		} catch (IOException | InterruptedException e) {
			log.error("좀비 컨테이너 삭제 실패: {}", containerName, e);
		}
	}
}
