package kr.java.coditor.domain.grade.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SubmissionRateLimiter {

	private static final Logger log = LoggerFactory.getLogger(SubmissionRateLimiter.class);
	private final StringRedisTemplate redisTemplate;

	// 60초 동안 2회까지 제출 가능하도록 설정
	private static final int MAX_REQUESTS = 2;
	private static final long TIME_WINDOW_SECONDS = 60;

	public SubmissionRateLimiter(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public boolean isAllowed(Long memberId) {
		String key = "rate_limit:submit:" + memberId;

		Long count = redisTemplate.opsForValue().increment(key);

		// 처음 제출에 타이머 작동
		if (count != null && count == 1) {
			redisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW_SECONDS));
		}

		// 요청 2회 초과시 차단
		if (count != null && count > MAX_REQUESTS) {
			log.warn("요청 허용량 초과! memberId: {}, 현재 요청 수: {}", memberId, count);
			return false;
		}

		return true;
	}
}
