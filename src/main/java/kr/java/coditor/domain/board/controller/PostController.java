package kr.java.coditor.domain.board.controller;

import jakarta.validation.Valid;
import kr.java.coditor.domain.board.dto.*;
import kr.java.coditor.domain.board.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/coditor/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	public ResponseEntity<PostResponse> createPost(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody PostCreateRequest request) {
		String email = userDetails.getUsername();
		log.info("게시글 등록 API 호출 - email: {}", email);
		PostResponse response = postService.createPost(email, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<Page<PostListResponse>> getPostList(
		@RequestParam(required = false) String keyword,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		log.info("게시글 목록 조회 API 호출 - keyword: {}", keyword);
		Page<PostListResponse> response = postService.getPostList(keyword, pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{postId}")
	public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
		log.info("게시글 상세 조회 API 호출 - postId: {}", postId);
		PostDetailResponse response = postService.getPostDetail(postId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{postId}")
	public ResponseEntity<PostResponse> updatePost(
		@PathVariable Long postId,
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody PostUpdateRequest request) {
		String email = userDetails.getUsername();
		log.info("게시글 수정 API 호출 - email: {}, postId: {}", email, postId);
		PostResponse response = postService.updatePost(email, postId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(
		@PathVariable Long postId,
		@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails.getUsername();
		log.info("게시글 삭제 API 호출 - email: {}, postId: {}", email, postId);
		postService.deletePost(email, postId);
		return ResponseEntity.noContent().build();
	}

}
