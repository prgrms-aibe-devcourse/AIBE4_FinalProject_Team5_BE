package kr.java.coditor.domain.problem.dto;

public record ProblemUpdateRequest(
	String title,
	String content,
	String inputDesc,
	String outputDesc,
	Integer level,
	Double timeLimit,
	Integer memoryLimit,
	Boolean isVisible
) {
}
