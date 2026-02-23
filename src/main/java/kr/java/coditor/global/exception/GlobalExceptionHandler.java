package kr.java.coditor.global.exception;

import kr.java.coditor.domain.problem.exception.ProblemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProblemException.class)
	public ResponseEntity<Map<String, String>> handleProblemException(ProblemException e) {
		log.warn("Problem Exception Occurred: {}", e.getErrorCode().getMessage());

		return ResponseEntity
			.status(e.getErrorCode().getHttpStatus())
			.body(Map.of("message", e.getErrorCode().getMessage()));
	}

	// 도메인 별 에러 추가
}
