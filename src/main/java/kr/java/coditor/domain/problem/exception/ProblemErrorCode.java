package kr.java.coditor.domain.problem.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProblemErrorCode {

	PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문제를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
	TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),
	TESTCASE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 테스트케이스입니다."),
	TESTCASE_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "입력 파일과 정답 파일의 개수가 일치하지 않습니다."),
	TESTCASE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "테스트케이스 파일은 한 번에 최대 10쌍까지만 업로드할 수 있습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
