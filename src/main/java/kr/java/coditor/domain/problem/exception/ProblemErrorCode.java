package kr.java.coditor.domain.problem.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProblemErrorCode {

	PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문제를 찾을 수 없습니다."),
	ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
