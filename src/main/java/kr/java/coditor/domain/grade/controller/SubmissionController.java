package kr.java.coditor.domain.grade.controller;

import kr.java.coditor.domain.grade.dto.CodeSubmissionRequest;
import kr.java.coditor.domain.grade.service.ScoringProducer;
import kr.java.coditor.global.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

	private final RabbitTemplate rabbitTemplate;

	public SubmissionController(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@PostMapping
	public ResponseEntity<String> submitCode(@RequestBody CodeSubmissionRequest request) {
		// 프론트엔드에서 받은 요청 객체를 RabbitMQ 채점 큐로 전송
		rabbitTemplate.convertAndSend(RabbitMqConfig.SCORING_QUEUE, request);

		return ResponseEntity.ok("코드가 성공적으로 제출되었습니다.");
	}
}
