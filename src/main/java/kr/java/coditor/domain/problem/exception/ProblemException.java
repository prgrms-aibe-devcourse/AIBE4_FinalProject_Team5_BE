package kr.java.coditor.domain.problem.exception;

import lombok.Getter;

@Getter
public class ProblemException extends RuntimeException {

	private final ProblemErrorCode errorCode;

	public ProblemException(ProblemErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
