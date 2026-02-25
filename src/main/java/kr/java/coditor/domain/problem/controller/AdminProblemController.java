package kr.java.coditor.domain.problem.controller;

import kr.java.coditor.domain.problem.dto.ProblemCreateRequest;
import kr.java.coditor.domain.problem.dto.ProblemResponse;
import kr.java.coditor.domain.problem.dto.ProblemUpdateRequest;
import kr.java.coditor.domain.problem.dto.TestCaseResponse;
import kr.java.coditor.domain.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

	// 테스트케이스 파일 업로드
	@PostMapping("/{id}/testcases")
	public ResponseEntity<Void> addTestCase(
		@PathVariable Long id,
		@RequestPart("inputFile") MultipartFile inputFile,
		@RequestPart("outputFile") MultipartFile outputFile) {
		Long mockAdminId = 1L;
		log.info("API 호출: [관리자] 테스트케이스 파일 업로드 요청 - ID: {}", id);
		problemService.addTestCase(mockAdminId, id, inputFile, outputFile);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	// 테스트케이스 파일 단건 삭제
	@DeleteMapping("/testcases/{testcaseId}")
	public ResponseEntity<Void> deleteTestCase(@PathVariable Long testcaseId) {
		Long mockAdminId = 1L;
		log.info("API 호출: [관리자] 테스트케이스 파일 삭제 요청 - ID: {}", testcaseId);
		problemService.deleteTestCase(mockAdminId, testcaseId);
		return ResponseEntity.noContent().build();
	}

	// 테스트케이스 목록 조회
	@GetMapping("/{id}/testcases")
	public ResponseEntity<List<TestCaseResponse>> getTestCases(@PathVariable Long id) {
		Long mockAdminId = 1L;
		log.info("API 호출: [관리자] 테스트케이스 목록 조회 요청 - ID: {}", id);
		List<TestCaseResponse> response = problemService.getTestCases(mockAdminId, id);
		return ResponseEntity.ok(response);
	}

}
