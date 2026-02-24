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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/coditor/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
		// TODO: 추후 JWT 로그인 기능 연동 시 수정
		Long dummyUserId = 1L;
		log.info("게시글 등록 API 호출 - 임시 userId: {}", dummyUserId);
		PostResponse response = postService.createPost(dummyUserId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<Page<PostListResponse>> getPostList(
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		log.info("게시글 목록 조회 API 호출");
		Page<PostListResponse> response = postService.getPostList(pageable);
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
		@Valid @RequestBody PostUpdateRequest request) {
		Long dummyUserId = 1L; // 임시
		log.info("게시글 수정 API 호출 - postId: {}", postId);
		PostResponse response = postService.updatePost(dummyUserId, postId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
		Long dummyUserId = 1L; // 임시
		log.info("게시글 삭제 API 호출 - postId: {}", postId);
		postService.deletePost(dummyUserId, postId);
		return ResponseEntity.noContent().build();
	}

}
