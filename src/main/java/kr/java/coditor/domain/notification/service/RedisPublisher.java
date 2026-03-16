package kr.java.coditor.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.java.coditor.domain.notification.dto.NotificationMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

	private final StringRedisTemplate stringRedisTemplate;
	private final ChannelTopic notificationTopic;
	private final ObjectMapper objectMapper;

	public RedisPublisher(
		StringRedisTemplate stringRedisTemplate,
		ChannelTopic notificationTopic,
		ObjectMapper objectMapper
	) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.notificationTopic = notificationTopic;
		this.objectMapper = objectMapper;
	}

	public void publish(NotificationMessage message) {
		try {
			String json = objectMapper.writeValueAsString(message);

			String key = notificationKey(message.getMemberId());

			stringRedisTemplate.opsForList().leftPush(key, json);
			stringRedisTemplate.opsForList().trim(key, 0, 49);

			stringRedisTemplate.convertAndSend(notificationTopic.getTopic(), json);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("알림 직렬화 실패", e);
		}
	}

	private String notificationKey(Long memberId) {
		return "notifications:" + memberId;
	}
}
