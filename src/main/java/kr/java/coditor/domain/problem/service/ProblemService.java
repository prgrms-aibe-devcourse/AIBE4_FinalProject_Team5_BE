package kr.java.coditor.domain.problem.service;

import kr.java.coditor.domain.problem.dto.*;
import kr.java.coditor.domain.problem.entity.*;
import kr.java.coditor.domain.problem.exception.ProblemErrorCode;
import kr.java.coditor.domain.problem.exception.ProblemException;
import kr.java.coditor.domain.problem.repository.ProblemRepository;
import kr.java.coditor.domain.problem.repository.TagRepository;
import kr.java.coditor.domain.problem.repository.TestCaseRepository;
import kr.java.coditor.domain.user.entity.Role;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.repository.UserRepository;
import kr.java.coditor.global.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

	private final ProblemRepository problemRepository;
	private final TagRepository tagRepository;
	private final UserRepository userRepository;
	private final TestCaseRepository testCaseRepository;
	private final S3Service s3Service;

	private void validateAdminRole(String adminEmail) {
		User user = userRepository.findByEmail(adminEmail)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.USER_NOT_FOUND));

		if (user.getRole() != Role.ADMIN) {
			log.warn("권한 없는 사용자의 접근 시도 - User Email: {}, 현재 Role: {}", adminEmail, user.getRole());
			throw new ProblemException(ProblemErrorCode.ADMIN_ACCESS_DENIED);
		}
	}
	/**
	 * [관리자] 문제 등록
	 */
	@Transactional
	public ProblemResponse createProblem(String adminEmail, ProblemCreateRequest request) {
		validateAdminRole(adminEmail);

		Problem problem = request.toEntity();

		if (request.examples() != null && !request.examples().isEmpty()) {
			for (var exDto : request.examples()) {
				ProblemExample example = ProblemExample.builder()
					.problem(problem)
					.inputExample(exDto.inputExample())
					.outputExample(exDto.outputExample())
					.build();
				problem.addExample(example);
			}
		}

		if (request.tags() != null && !request.tags().isEmpty()) {
			for (String tagName : request.tags()) {
				Tag tag = tagRepository.findByName(tagName)
					.orElseThrow(() -> new ProblemException(ProblemErrorCode.TAG_NOT_FOUND));

				ProblemTag problemTag = new ProblemTag(problem, tag);
				problem.addProblemTag(problemTag);
			}
		}

		problem = problemRepository.save(problem);
		log.info("새로운 문제 등록 완료 - Problem ID: {}, Title: {}", problem.getId(), problem.getTitle());
		return ProblemResponse.from(problem);
	}

	/**
	 * [공통] 문제 단건 상세 조회
	 */
	@Transactional(readOnly = true)
	public ProblemResponse getProblem(Long id) {
		Problem problem = problemRepository.findById(id)
			.orElseThrow(() -> {
				log.warn("문제 조회 실패 - 존재하지 않는 Problem ID: {}", id);
				return new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND);
			});

		return ProblemResponse.from(problem);
	}

	/**
	 * [공통] 문제 전체 목록 조회
	 */
	@Transactional(readOnly = true)
	public Page<ProblemResponse> getAllProblems(String keyword, Integer level, String tag, Pageable pageable) {
		Page<Problem> problemPage = problemRepository.searchProblems(keyword, level, tag, pageable);

		log.info("문제 목록 페이징 검색 결과 - 총 {}페이지 중 {}번째 페이지", problemPage.getTotalPages(), problemPage.getNumber());

		return problemPage.map(ProblemResponse::from);
	}

	/**
	 * [관리자] 문제 부분 수정
	 */
	@Transactional
	public ProblemResponse updateProblem(String adminEmail, Long problemId, ProblemUpdateRequest request) {
		validateAdminRole(adminEmail);

		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND));

		problem.update(
			request.title(),
			request.content(),
			request.inputDesc(),
			request.outputDesc(),
			request.level(),
			request.timeLimit(),
			request.memoryLimit(),
			request.isVisible()
		);

		if (request.examples() != null) {
			List<ProblemExample> newExamples = request.examples().stream()
				.map(exDto -> ProblemExample.builder()
					.problem(problem)
					.inputExample(exDto.inputExample())
					.outputExample(exDto.outputExample())
					.build())
				.collect(Collectors.toList());

			problem.updateExamples(newExamples);
		}

		if (request.tags() != null) {
			List<ProblemTag> newProblemTags = request.tags().stream()
				.map(tagName -> {
					Tag tag = tagRepository.findByName(tagName)
						.orElseThrow(() -> new ProblemException(ProblemErrorCode.TAG_NOT_FOUND));
					return new ProblemTag(problem, tag);
				})
				.collect(Collectors.toList());

			problem.updateProblemTags(newProblemTags);
		}

		log.info("문제 수정 완료 - Problem ID: {}", problem.getId());
		return ProblemResponse.from(problem);
	}

	/**
	 * [관리자] 문제 삭제
	 */
	@Transactional
	public void deleteProblem(String adminEmail, Long problemId) {
		validateAdminRole(adminEmail);

		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND));

		for (TestCase testCase : problem.getTestCases()) {
			s3Service.deleteFile(testCase.getInputUrl());
			s3Service.deleteFile(testCase.getOutputUrl());
		}

		problemRepository.delete(problem);
		log.info("문제 및 테스트케이스 파일 삭제 완료 - Problem ID: {}", problemId);
	}

	@Transactional(readOnly = true)
	public List<TagResponse> getAllTags() {
		List<Tag> tags = tagRepository.findAll();
		log.info("전체 태그 목록 조회 - 총 {}건", tags.size());
		return tags.stream()
			.map(TagResponse::from)
			.collect(Collectors.toList());
	}

	/**
	 * [관리자] 특정 문제에 테스트케이스 파일 추가
	 */
	@Transactional
	public void addTestCase(String adminEmail, Long problemId, MultipartFile inputFile, MultipartFile outputFile) {
		validateAdminRole(adminEmail);

		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND));

		String inputUrl = s3Service.uploadFile(inputFile, "testcases/inputs");
		String outputUrl = s3Service.uploadFile(outputFile, "testcases/outputs");
		//String inputUrl = "https://dummy-s3-url.com/input.txt";
		//String outputUrl = "https://dummy-s3-url.com/output.txt";

		TestCase testCase = TestCase.builder()
			.problem(problem)
			.inputUrl(inputUrl)
			.outputUrl(outputUrl)
			.build();

		problem.addTestCase(testCase);
		testCase = testCaseRepository.save(testCase);
		log.info("테스트케이스 추가 완료 - Problem ID: {}, TestCase ID: {}", problemId, testCase.getId());
	}

	/**
	 * [관리자] 특정 테스트케이스 단건 삭제
	 */
	@Transactional
	public void deleteTestCase(String adminEmail, Long testCaseId) {
		validateAdminRole(adminEmail);

		TestCase testCase = testCaseRepository.findById(testCaseId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.TESTCASE_NOT_FOUND));

		s3Service.deleteFile(testCase.getInputUrl());
		s3Service.deleteFile(testCase.getOutputUrl());

		testCaseRepository.delete(testCase);

		log.info("테스트케이스 삭제 완료 - TestCase ID: {}", testCaseId);
	}

	@Transactional(readOnly = true)
	public List<TestCaseResponse> getTestCases(String adminEmail, Long problemId) {
		validateAdminRole(adminEmail);

		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND));

		List<TestCase> testCases = problem.getTestCases();
		log.info("테스트케이스 목록 조회 - Problem ID: {}, 총 {}건", problemId, testCases.size());

		return testCases.stream()
			.map(TestCaseResponse::from)
			.collect(Collectors.toList());
	}

}
