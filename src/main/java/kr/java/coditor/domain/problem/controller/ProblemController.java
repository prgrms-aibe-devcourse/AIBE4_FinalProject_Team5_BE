package kr.java.coditor.domain.problem.controller;

import kr.java.coditor.domain.problem.dto.ProblemListResponse;
import kr.java.coditor.domain.problem.dto.ProblemResponse;
import kr.java.coditor.domain.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/coditor/problems")
@RequiredArgsConstructor
public class ProblemController {

	private final ProblemService problemService;

	// 전체 문제 목록 조회
	@GetMapping
	public ResponseEntity<Page<ProblemListResponse>> getAllProblems(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) Integer level,
		@RequestParam(required = false) String tag,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(problemService.getAllProblems(keyword, level, tag, pageable));
	}

	// 문제 상세 조회
	@GetMapping("/{id}")
	public ResponseEntity<ProblemResponse> getProblem(@PathVariable Long id) {
		log.info("API 호출: 문제 상세 조회 - ID: {}", id);
		return ResponseEntity.ok(problemService.getProblem(id));
	}
}
