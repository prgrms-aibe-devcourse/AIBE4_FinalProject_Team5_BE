package kr.java.coditor.domain.grade.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class GradeResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long problemId;
	private Long memberId;
	private String status;

	@Lob
	private String outputMessage;

	protected GradeResult() {}

	public GradeResult(Long problemId, Long memberId, String status, String outputMessage) {
		this.problemId = problemId;
		this.memberId = memberId;
		this.status = status;
		this.outputMessage = outputMessage;
	}

	public Long getId() {
		return id;
	}

	public Long getProblemId() {
		return problemId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getStatus() {
		return status;
	}

	public String getOutputMessage() {
		return outputMessage;
	}
}
