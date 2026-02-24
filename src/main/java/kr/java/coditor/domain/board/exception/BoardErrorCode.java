package kr.java.coditor.domain.board.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BoardErrorCode {

	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
	PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문제를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "해당 게시글에 대한 권한이 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "부모 댓글이 해당 게시글에 존재하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
