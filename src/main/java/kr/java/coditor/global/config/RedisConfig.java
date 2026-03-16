package kr.java.coditor.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import kr.java.coditor.domain.notification.service.RedisSubscriber;

@Configuration
public class RedisConfig {

	@Bean
	public ChannelTopic notificationTopic() {
		return new ChannelTopic("notification-channel");
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory connectionFactory,
		RedisSubscriber redisSubscriber,
		ChannelTopic notificationTopic
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(redisSubscriber, notificationTopic);
		return container;
	}
}
