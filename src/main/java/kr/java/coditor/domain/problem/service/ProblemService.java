package kr.java.coditor.domain.problem.service;

import kr.java.coditor.domain.problem.dto.ProblemCreateRequest;
import kr.java.coditor.domain.problem.dto.ProblemResponse;
import kr.java.coditor.domain.problem.entity.Problem;
import kr.java.coditor.domain.problem.repository.ProblemRepository;
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

	/**
	 * [관리자] 문제 등록
	 */
	@Transactional
	public ProblemResponse createProblem(Long adminId, ProblemCreateRequest request) {
		// TODO: 추후 커스텀 예외로 변경 & Security 적용 시 DB 유저 권한(ADMIN) 조회 로직 추가 예정
		if (!adminId.equals(1L)) {
			log.warn("권한 없는 사용자의 문제 등록 시도 - User ID: {}", adminId);
			throw new IllegalStateException("관리자만 접근할 수 있습니다.");
		}
		Problem problem = problemRepository.save(request.toEntity());
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
				return new IllegalArgumentException("해당 문제를 찾을 수 없습니다. ID: " + id);
			});

		return ProblemResponse.from(problem);
	}

	/**
	 * [공통] 문제 전체 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<ProblemResponse> getAllProblems() {
		List<Problem> problems = problemRepository.findAll();
		log.debug("전체 문제 목록 조회 - 총 {}건", problems.size());
		return problems.stream()
			.map(ProblemResponse::from)
			.collect(Collectors.toList());
	}
}
