package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.Problem;
import java.util.List;
import java.util.stream.Collectors;

public record ProblemListResponse(
	Long id,
	String title,
	Integer level,
	List<String> tags
) {
	public static ProblemListResponse from(Problem problem) {
		return new ProblemListResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getLevel(),
			problem.getProblemTags().stream()
				.map(problemTag -> problemTag.getTag().getName())
				.collect(Collectors.toList())
		);
	}
}
