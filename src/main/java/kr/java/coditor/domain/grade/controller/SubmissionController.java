package kr.java.coditor.domain.grade.controller;

import kr.java.coditor.domain.grade.dto.CodeSubmissionRequest;
import kr.java.coditor.domain.grade.service.SubmissionRateLimiter;
import kr.java.coditor.global.config.RabbitMqConfig; // 설정 파일 임포트
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

	private final SubmissionRateLimiter rateLimiter;
	private final RabbitTemplate rabbitTemplate;

	public SubmissionController(SubmissionRateLimiter rateLimiter, RabbitTemplate rabbitTemplate) {
		this.rateLimiter = rateLimiter;
		this.rabbitTemplate = rabbitTemplate;
	}

	@PostMapping
	public ResponseEntity<?> submitCode(@RequestBody CodeSubmissionRequest request) {

		// 60초 내에 3회이ㅣ상 요청시 차단
		if (!rateLimiter.isAllowed(request.getMemberId())) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body("요청이 너무 많습니다. 10초 후에 다시 시도해주세요.");
		}

		rabbitTemplate.convertAndSend(RabbitMqConfig.SCORING_EXCHANGE, RabbitMqConfig.SCORING_ROUTING_KEY, request);

		return ResponseEntity.ok().body("코드가 성공적으로 제출되었습니다.");
	}
}
