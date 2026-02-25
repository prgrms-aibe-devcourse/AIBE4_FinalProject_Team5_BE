package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.TestCase;

public record TestCaseResponse(
	Long id,
	String inputUrl,
	String outputUrl
) {
	public static TestCaseResponse from(TestCase testCase) {
		return new TestCaseResponse(
			testCase.getId(),
			testCase.getInputUrl(),
			testCase.getOutputUrl()
		);
	}
}
