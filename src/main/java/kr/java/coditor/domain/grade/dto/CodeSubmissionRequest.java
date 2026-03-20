package kr.java.coditor.domain.grade.dto;

public class CodeSubmissionRequest {

	private Long problemId;
	private Long memberId;
	private String language;
	private String sourceCode;
	private String persona;

	public CodeSubmissionRequest() {}

	public CodeSubmissionRequest(Long problemId, Long memberId, String language, String sourceCode) {
		this.problemId = problemId;
		this.memberId = memberId;
		this.language = language;
		this.sourceCode = sourceCode;
		this.persona = null;
	}

	public Long getProblemId() {
		return problemId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getLanguage() {
		return language;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public String getPersona() { return persona; }
}
