package kr.java.coditor.domain.notification.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import kr.java.coditor.domain.notification.dto.NotificationMessage;

@Service
public class RedisPublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic notificationTopic;

	public RedisPublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic notificationTopic) {
		this.redisTemplate = redisTemplate;
		this.notificationTopic = notificationTopic;
	}

	public void publish(NotificationMessage message) {
		redisTemplate.convertAndSend(notificationTopic.getTopic(), message);
	}
}
