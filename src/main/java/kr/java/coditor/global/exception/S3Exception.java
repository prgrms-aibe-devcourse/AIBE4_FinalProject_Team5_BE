package kr.java.coditor.global.exception;

import lombok.Getter;

@Getter
public class S3Exception extends RuntimeException {
	private final S3ErrorCode errorCode;

	public S3Exception(S3ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
