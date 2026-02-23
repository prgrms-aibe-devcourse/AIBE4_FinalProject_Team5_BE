package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.Problem;

public record ProblemCreateRequest(
	String title,
	String content,
	String inputDesc,
	String outputDesc,
	Integer level,
	Double timeLimit,
	Integer memoryLimit,
	Boolean isVisible
) {
	public Problem toEntity() {
		return Problem.builder()
			.title(title)
			.content(content)
			.inputDesc(inputDesc)
			.outputDesc(outputDesc)
			.level(level)
			.timeLimit(timeLimit)
			.memoryLimit(memoryLimit)
			.isVisible(isVisible)
			.build();
	}
}
