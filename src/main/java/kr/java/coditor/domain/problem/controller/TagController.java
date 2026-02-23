package kr.java.coditor.domain.problem.controller;

import kr.java.coditor.domain.problem.dto.TagResponse;
import kr.java.coditor.domain.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coditor/tags")
@RequiredArgsConstructor
public class TagController {

	private final ProblemService problemService;

	/**
	 * 전체 태그 목록 조회 API
	 */
	@GetMapping
	public ResponseEntity<List<TagResponse>> getAllTags() {
		List<TagResponse> tags = problemService.getAllTags();
		return ResponseEntity.ok(tags);
	}
}
