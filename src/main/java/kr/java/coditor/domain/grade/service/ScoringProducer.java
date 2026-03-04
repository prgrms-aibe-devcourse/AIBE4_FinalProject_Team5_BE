package kr.java.coditor.domain.grade.service;

import kr.java.coditor.domain.grade.dto.CodeSubmissionRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import kr.java.coditor.global.config.RabbitMqConfig;

@Service
public class ScoringProducer {

	private final RabbitTemplate rabbitTemplate;

	public ScoringProducer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void sendScoringRequest(CodeSubmissionRequest request) {
		rabbitTemplate.convertAndSend(
			RabbitMqConfig.SCORING_EXCHANGE,
			RabbitMqConfig.SCORING_ROUTING_KEY,
			request
		);
	}
}
