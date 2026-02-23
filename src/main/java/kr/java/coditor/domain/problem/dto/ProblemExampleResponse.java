package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.ProblemExample;

public record ProblemExampleResponse(
	Long id,
	String inputExample,
	String outputExample
) {
	public static ProblemExampleResponse from(ProblemExample example) {
		return new ProblemExampleResponse(
			example.getId(),
			example.getInputExample(),
			example.getOutputExample()
		);
	}
}
