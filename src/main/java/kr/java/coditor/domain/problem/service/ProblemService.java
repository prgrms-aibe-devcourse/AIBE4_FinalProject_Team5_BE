package kr.java.coditor.domain.problem.service;

import kr.java.coditor.domain.problem.dto.ProblemCreateRequest;
import kr.java.coditor.domain.problem.dto.ProblemResponse;
import kr.java.coditor.domain.problem.dto.ProblemUpdateRequest;
import kr.java.coditor.domain.problem.dto.TagResponse;
import kr.java.coditor.domain.problem.entity.Problem;
import kr.java.coditor.domain.problem.entity.ProblemExample;
import kr.java.coditor.domain.problem.entity.ProblemTag;
import kr.java.coditor.domain.problem.entity.Tag;
import kr.java.coditor.domain.problem.exception.ProblemErrorCode;
import kr.java.coditor.domain.problem.exception.ProblemException;
import kr.java.coditor.domain.problem.repository.ProblemRepository;
import kr.java.coditor.domain.problem.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

	private final ProblemRepository problemRepository;

	private final TagRepository tagRepository;
	/**
	 * [관리자] 문제 등록
	 */
	@Transactional
	public ProblemResponse createProblem(Long adminId, ProblemCreateRequest request) {
		// TODO: Security 적용 시 DB 유저 권한(ADMIN) 조회 로직 추가
		if (!adminId.equals(1L)) {
			log.warn("권한 없는 사용자의 문제 등록 시도 - User ID: {}", adminId);
			throw new ProblemException(ProblemErrorCode.ADMIN_ACCESS_DENIED);
		}
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
	public List<ProblemResponse> getAllProblems() {
		List<Problem> problems = problemRepository.findAll();
		log.info("전체 문제 목록 조회 - 총 {}건", problems.size());
		return problems.stream()
			.map(ProblemResponse::from)
			.collect(Collectors.toList());
	}

	/**
	 * [관리자] 문제 부분 수정
	 */
	@Transactional
	public ProblemResponse updateProblem(Long adminId, Long problemId, ProblemUpdateRequest request) {
		// 임시 권한 체크
		if (!adminId.equals(1L)) {
			log.warn("권한 없는 사용자의 문제 수정 시도 - User ID: {}", adminId);
			throw new ProblemException(ProblemErrorCode.ADMIN_ACCESS_DENIED);
		}
		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND));
		// 부분 업데이트 수행
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
	public void deleteProblem(Long adminId, Long problemId) {
		// 임시 권한 체크
		if (!adminId.equals(1L)) {
			log.warn("권한 없는 사용자의 문제 삭제 시도 - User ID: {}", adminId);
			throw new ProblemException(ProblemErrorCode.ADMIN_ACCESS_DENIED);
		}
		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemException(ProblemErrorCode.PROBLEM_NOT_FOUND));
		problemRepository.delete(problem);
		log.info("문제 삭제 완료 - Problem ID: {}", problemId);
	}

	@Transactional(readOnly = true)
	public List<TagResponse> getAllTags() {
		List<Tag> tags = tagRepository.findAll();
		log.info("전체 태그 목록 조회 - 총 {}건", tags.size());
		return tags.stream()
			.map(TagResponse::from)
			.collect(Collectors.toList());
	}
}
