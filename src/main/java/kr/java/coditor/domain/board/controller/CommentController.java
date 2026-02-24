package kr.java.coditor.domain.board.controller;

import jakarta.validation.Valid;
import kr.java.coditor.domain.board.dto.CommentCreateRequest;
import kr.java.coditor.domain.board.dto.CommentResponse;
import kr.java.coditor.domain.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/coditor/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentCreateRequest request) {
		// TODO: 추후 JWT 로그인 기능 연동 시 수정
		Long dummyUserId = 1L;
		log.info("댓글 등록 API 호출 - 임시 userId: {}, postId: {}", dummyUserId, request.postId());
		CommentResponse response = commentService.createComment(dummyUserId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
