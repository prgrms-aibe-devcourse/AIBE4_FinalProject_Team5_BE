package kr.java.coditor.domain.problem.controller;

import kr.java.coditor.domain.problem.dto.ProblemCreateRequest;
import kr.java.coditor.domain.problem.dto.ProblemResponse;
import kr.java.coditor.domain.problem.dto.ProblemUpdateRequest;
import kr.java.coditor.domain.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/coditor/admin/problems")
@RequiredArgsConstructor
public class AdminProblemController {

	private final ProblemService problemService;

	// 문제 등록
	@PostMapping
	public ResponseEntity<ProblemResponse> createProblem(@RequestBody ProblemCreateRequest request) {
		Long mockAdminId = 1L;
		log.info("API 호출: [관리자] 문제 등록 요청 - Title: {}, UserID: {}", request.title(), mockAdminId);
		ProblemResponse response = problemService.createProblem(mockAdminId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 문제 부분 수정
	@PatchMapping("/{id}")
	public ResponseEntity<ProblemResponse> updateProblem(
		@PathVariable Long id,
		@RequestBody ProblemUpdateRequest request) {
		Long mockAdminId = 1L;
		log.info("API 호출: [관리자] 문제 부분 수정 요청 - ID: {}", id);
		ProblemResponse response = problemService.updateProblem(mockAdminId, id, request);
		return ResponseEntity.ok(response);
	}

	// 문제 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
		Long mockAdminId = 1L;
		log.info("API 호출: [관리자] 문제 삭제 요청 - ID: {}", id);
		problemService.deleteProblem(mockAdminId, id);
		return ResponseEntity.noContent().build();
	}
}
