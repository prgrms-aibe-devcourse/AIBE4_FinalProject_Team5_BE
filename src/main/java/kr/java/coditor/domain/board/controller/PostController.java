package kr.java.coditor.domain.board.controller;

import jakarta.validation.Valid;
import kr.java.coditor.domain.board.dto.PostCreateRequest;
import kr.java.coditor.domain.board.dto.PostResponse;
import kr.java.coditor.domain.board.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
