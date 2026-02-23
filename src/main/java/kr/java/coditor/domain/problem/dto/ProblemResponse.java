package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.Problem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ProblemResponse(
	Long id,
	String title,
	String content,
	String inputDesc,
	String outputDesc,
	Integer level,
	Double timeLimit,
	Integer memoryLimit,
	Boolean isVisible,
	LocalDateTime createdAt,
	List<ProblemExampleResponse> examples
) {
	public static ProblemResponse from(Problem problem) {
		return new ProblemResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getInputDesc(),
			problem.getOutputDesc(),
			problem.getLevel(),
			problem.getTimeLimit(),
			problem.getMemoryLimit(),
			problem.getIsVisible(),
			problem.getCreatedAt(),
			problem.getExamples().stream()
				.map(ProblemExampleResponse::from)
				.collect(Collectors.toList())
		);
	}
}
