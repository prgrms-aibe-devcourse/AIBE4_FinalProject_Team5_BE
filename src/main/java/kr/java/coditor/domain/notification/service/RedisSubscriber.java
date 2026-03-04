package kr.java.coditor.domain.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.java.coditor.domain.notification.dto.NotificationMessage;

@Service
public class RedisSubscriber {

	private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

	// 직접 인스턴스화하여 사용
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final NotificationService notificationService;

	// 생성자에서 ObjectMapper를 제거하고 NotificationService만 주입받음
	public RedisSubscriber(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void sendMessage(String publishMessage) {
		try {
			NotificationMessage message = objectMapper.readValue(publishMessage, NotificationMessage.class);
			notificationService.sendToClient(message.getMemberId(), message.getMessage());
		} catch (Exception e) {
			log.error("Redis 메시지 역직렬화 및 전송 실패", e);
		}
	}
}
