package kr.java.coditor.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode {
	EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어있습니다."),
	FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드 중 오류가 발생했습니다."),
	FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
