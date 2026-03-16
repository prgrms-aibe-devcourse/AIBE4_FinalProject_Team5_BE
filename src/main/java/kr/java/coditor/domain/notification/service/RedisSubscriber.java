package kr.java.coditor.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.java.coditor.domain.notification.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class RedisSubscriber implements MessageListener {

	private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

	private final ObjectMapper objectMapper;
	private final NotificationService notificationService;

	public RedisSubscriber(ObjectMapper objectMapper, NotificationService notificationService) {
		this.objectMapper = objectMapper;
		this.notificationService = notificationService;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
			NotificationMessage notificationMessage =
				objectMapper.readValue(publishMessage, NotificationMessage.class);

			notificationService.sendToClient(notificationMessage);
		} catch (Exception e) {
			log.error("Redis 메시지 역직렬화 및 SSE 전송 실패", e);
		}
	}
}
