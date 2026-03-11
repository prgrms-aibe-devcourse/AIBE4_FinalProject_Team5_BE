package kr.java.coditor.domain.user.service;

import kr.java.coditor.domain.grade.entity.Submit;
import kr.java.coditor.domain.grade.repository.SubmitRepository;
import kr.java.coditor.domain.problem.entity.Problem;
import kr.java.coditor.domain.problem.repository.ProblemRepository;
import kr.java.coditor.domain.user.dto.ActivityDto;
import kr.java.coditor.domain.user.dto.SolvedProblemDto;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPageService {

	private final UserRepository userRepository;
	private final SubmitRepository submitRepository;
	private final ProblemRepository problemRepository;

	@Transactional(readOnly = true)
	public List<ActivityDto> getUserActivity() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found with email: " + email));

		return submitRepository.findUserActivity(user.getId());
	}

	@Transactional(readOnly = true)
	public List<SolvedProblemDto> getSolvedProblems() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found with email: " + email));

		// 1. 유저가 해결한 문제 (result = 'CORRECT') 목록 조회
		List<Submit> solvedSubmits = submitRepository.findByUserIdAndResult(user.getId(), "CORRECT");

		// 중복 제거 및 최신 제출 기준으로 필터링 (문제 ID별로 가장 최근에 해결한 제출만 남김)
		Map<Long, Submit> distinctSolvedProblems = solvedSubmits.stream()
			.collect(Collectors.toMap(
				Submit::getProblemId,
				submit -> submit,
				(existing, replacement) -> existing.getCreatedAt().isAfter(replacement.getCreatedAt()) ? existing : replacement
			));

		Set<Long> problemIds = distinctSolvedProblems.keySet();

		// 2. 문제 ID를 사용하여 Problem 엔티티에서 문제 정보 조회
		List<Problem> problems = problemRepository.findAllByIdIn(problemIds.stream().collect(Collectors.toList()));
		Map<Long, Problem> problemMap = problems.stream()
			.collect(Collectors.toMap(Problem::getId, problem -> problem));

		// 3. SolvedProblemDto로 변환
		return distinctSolvedProblems.values().stream()
			.map(submit -> {
				Problem problem = problemMap.get(submit.getProblemId());
				if (problem == null) {
					return null; // 문제 정보가 없는 경우 스킵
				}
				return new SolvedProblemDto(
					problem.getId(),
					problem.getTitle(),
					problem.getLevel(),
					submit.getCreatedAt().toLocalDate(), // 제출 시간을 날짜로 변환
					submit.getLanguage()
				);
			})
			.filter(dto -> dto != null)
			.sorted(Comparator.comparing(SolvedProblemDto::getSolvedAt).reversed()) // 최신순 정렬
			.collect(Collectors.toList());
	}
}
