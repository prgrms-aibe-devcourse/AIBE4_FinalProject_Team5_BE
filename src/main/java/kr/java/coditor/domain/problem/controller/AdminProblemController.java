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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
	public ResponseEntity<ProblemResponse> createProblem(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestBody ProblemCreateRequest request) {
		String adminEmail = userDetails.getUsername();
		log.info("API 호출: [관리자] 문제 등록 요청 - Title: {}, AdminEmail: {}", request.title(), adminEmail);
		ProblemResponse response = problemService.createProblem(adminEmail, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 문제 부분 수정
	@PatchMapping("/{id}")
	public ResponseEntity<ProblemResponse> updateProblem(
		@PathVariable Long id,
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestBody ProblemUpdateRequest request) {
		String adminEmail = userDetails.getUsername();
		log.info("API 호출: [관리자] 문제 부분 수정 요청 - ID: {}, AdminEmail: {}", id, adminEmail);
		ProblemResponse response = problemService.updateProblem(adminEmail, id, request);
		return ResponseEntity.ok(response);
	}

	// 문제 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProblem(
		@PathVariable Long id,
		@AuthenticationPrincipal UserDetails userDetails) {
		String adminEmail = userDetails.getUsername();
		log.info("API 호출: [관리자] 문제 삭제 요청 - ID: {}, AdminEmail: {}", id, adminEmail);
		problemService.deleteProblem(adminEmail, id);
		return ResponseEntity.noContent().build();
	}

	// 테스트케이스 파일 업로드
	@PostMapping("/{id}/testcases")
	public ResponseEntity<Void> addTestCases(
		@PathVariable Long id,
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestPart("inputFiles") List<MultipartFile> inputFiles,
		@RequestPart("outputFiles") List<MultipartFile> outputFiles) {
		String adminEmail = userDetails.getUsername();
		log.info("API 호출: [관리자] 테스트케이스 파일 업로드 요청 - ID: {}, AdminEmail: {}, Input 파일 수: {}, Output 파일 수: {}",
			id, adminEmail, inputFiles.size(), outputFiles.size());
		problemService.addTestCases(adminEmail, id, inputFiles, outputFiles);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	// 테스트케이스 파일 단건 삭제
	@DeleteMapping("/testcases/{testcaseId}")
	public ResponseEntity<Void> deleteTestCase(
		@PathVariable Long testcaseId,
		@AuthenticationPrincipal UserDetails userDetails) {
		String adminEmail = userDetails.getUsername();
		log.info("API 호출: [관리자] 테스트케이스 파일 삭제 요청 - ID: {}, AdminEmail: {}", testcaseId, adminEmail);
		problemService.deleteTestCase(adminEmail, testcaseId);
		return ResponseEntity.noContent().build();
	}

	// 테스트케이스 목록 조회
	@GetMapping("/{id}/testcases")
	public ResponseEntity<List<TestCaseResponse>> getTestCases(
		@PathVariable Long id,
		@AuthenticationPrincipal UserDetails userDetails) {
		String adminEmail = userDetails.getUsername();
		log.info("API 호출: [관리자] 테스트케이스 목록 조회 요청 - ID: {}, AdminEmail: {}", id, adminEmail);
		List<TestCaseResponse> response = problemService.getTestCases(adminEmail, id);
		return ResponseEntity.ok(response);
	}

}
