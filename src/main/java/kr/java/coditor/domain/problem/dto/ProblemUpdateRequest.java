package kr.java.coditor.domain.problem.dto;

import java.util.List;

public record ProblemUpdateRequest(
	String title,
	String content,
	String inputDesc,
	String outputDesc,
	Integer level,
	Double timeLimit,
	Integer memoryLimit,
	Boolean isVisible,
	List<ProblemExampleRequest> examples
) {
}
