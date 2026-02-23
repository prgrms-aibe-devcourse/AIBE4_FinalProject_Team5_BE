package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.Problem;

import java.util.List;

public record ProblemCreateRequest(
	String title,
	String content,
	String inputDesc,
	String outputDesc,
	Integer level,
	Double timeLimit,
	Integer memoryLimit,
	Boolean isVisible,
	List<ProblemExampleRequest> examples,
	List<String> tags
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
