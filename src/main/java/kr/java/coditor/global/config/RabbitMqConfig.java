package kr.java.coditor.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String SCORING_EXCHANGE = "scoring.exchange";
	public static final String SCORING_QUEUE = "scoring.queue";
	public static final String SCORING_ROUTING_KEY = "scoring.route";

	@Bean
	public DirectExchange scoringExchange() {
		return new DirectExchange(SCORING_EXCHANGE);
	}

	@Bean
	public Queue scoringQueue() {
		return new Queue(SCORING_QUEUE, true);
	}

	@Bean
	public Binding scoringBinding(Queue scoringQueue, DirectExchange scoringExchange) {
		return BindingBuilder.bind(scoringQueue).to(scoringExchange).with(SCORING_ROUTING_KEY);
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
