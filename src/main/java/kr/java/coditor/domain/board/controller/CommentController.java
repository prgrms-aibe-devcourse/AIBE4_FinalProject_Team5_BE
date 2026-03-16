package kr.java.coditor.domain.board.controller;

import jakarta.validation.Valid;
import kr.java.coditor.domain.board.dto.CommentCreateRequest;
import kr.java.coditor.domain.board.dto.CommentReadResponse;
import kr.java.coditor.domain.board.dto.CommentResponse;
import kr.java.coditor.domain.board.dto.CommentUpdateRequest;
import kr.java.coditor.domain.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/coditor/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<CommentResponse> createComment(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody CommentCreateRequest request) {
		String email = userDetails.getUsername();
		log.info("댓글 등록 API 호출 - email: {}, postId: {}", email, request.postId());
		CommentResponse response = commentService.createComment(email, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/post/{postId}")
	public ResponseEntity<List<CommentReadResponse>> getComments(@PathVariable Long postId) {
		log.info("댓글 목록 조회 API 호출 - postId: {}", postId);
		List<CommentReadResponse> response = commentService.getComments(postId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{commentId}")
	public ResponseEntity<CommentResponse> updateComment(
		@PathVariable Long commentId,
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody CommentUpdateRequest request) {
		String email = userDetails.getUsername();
		log.info("댓글 수정 API 호출 - email: {}, commentId: {}", email, commentId);
		CommentResponse response = commentService.updateComment(email, commentId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(
		@PathVariable Long commentId,
		@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails.getUsername();
		log.info("댓글 삭제 API 호출 - email: {}, commentId: {}", email, commentId);
		commentService.deleteComment(email, commentId);
		return ResponseEntity.noContent().build();
	}

}
