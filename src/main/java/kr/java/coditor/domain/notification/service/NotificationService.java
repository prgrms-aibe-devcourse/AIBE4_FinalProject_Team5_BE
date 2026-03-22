package kr.java.coditor.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.java.coditor.domain.notification.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	private static final Long DEFAULT_TIMEOUT = 60L * 60 * 1000;

	private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;

	public NotificationService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
	}

	public SseEmitter subscribe(Long memberId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		String emitterId = UUID.randomUUID().toString();

		emitters
			.computeIfAbsent(memberId, key -> new ConcurrentHashMap<>())
			.put(emitterId, emitter);

		emitter.onCompletion(() -> removeEmitter(memberId, emitterId));
		emitter.onTimeout(() -> removeEmitter(memberId, emitterId));
		emitter.onError(e -> removeEmitter(memberId, emitterId));

		try {
			emitter.send(
				SseEmitter.event()
					.name("connect")
					.data("connected")
			);
		} catch (Exception e) {
			removeEmitter(memberId, emitterId);
			log.info("SSE 연결 초기화 중 클라이언트 연결 종료. memberId={}", memberId);
		}

		return emitter;
	}

	public void sendToClient(NotificationMessage message) {
		Long memberId = message.getMemberId();

		if (memberId == null) {
			return;
		}

		Map<String, SseEmitter> userEmitters = emitters.get(memberId);

		if (userEmitters == null || userEmitters.isEmpty()) {
			return;
		}

		List<String> deadEmitters = new ArrayList<>();

		userEmitters.forEach((emitterId, emitter) -> {
			try {
				emitter.send(
					SseEmitter.event()
						.name("notification")
						.data(message)
				);
			} catch (Exception e) {
				deadEmitters.add(emitterId);
				log.info("SSE 연결이 종료된 클라이언트 (memberId: {}) - 메모리에서 제거됨", memberId);
			}
		});

		deadEmitters.forEach(emitterId -> removeEmitter(memberId, emitterId));
	}

	public List<NotificationMessage> getNotifications(Long memberId) {
		List<String> values = stringRedisTemplate.opsForList().range(notificationKey(memberId), 0, 49);

		if (values == null || values.isEmpty()) {
			return Collections.emptyList();
		}

		return values.stream()
			.map(this::readValue)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	public boolean markAsRead(Long memberId, String notificationId) {
		String key = notificationKey(memberId);
		List<String> values = stringRedisTemplate.opsForList().range(key, 0, -1);

		if (values == null || values.isEmpty()) {
			return false;
		}

		for (int i = 0; i < values.size(); i++) {
			NotificationMessage message = readValue(values.get(i));

			if (message == null) {
				continue;
			}

			if (notificationId.equals(message.getId())) {
				message.setRead(true);
				stringRedisTemplate.opsForList().set(key, i, writeValue(message));
				return true;
			}
		}

		return false;
	}

	public void markAllAsRead(Long memberId) {
		String key = notificationKey(memberId);

		List<String> values = stringRedisTemplate.opsForList().range(key, 0, -1);

		if (values == null || values.isEmpty()) {
			return;
		}

		for (int i = 0; i < values.size(); i++) {
			NotificationMessage message = readValue(values.get(i));

			if (message == null) {
				continue;
			}

			if (!message.isRead()) {
				message.setRead(true);
				stringRedisTemplate.opsForList().set(key, i, writeValue(message));
			}
		}
	}

	private NotificationMessage readValue(String json) {
		try {
			return objectMapper.readValue(json, NotificationMessage.class);
		} catch (Exception e) {
			log.error("알림 JSON 파싱 실패", e);
			return null;
		}
	}

	private String writeValue(NotificationMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("알림 JSON 직렬화 실패", e);
		}
	}

	private String notificationKey(Long memberId) {
		return "notifications:" + memberId;
	}

	private void removeEmitter(Long memberId, String emitterId) {
		Map<String, SseEmitter> userEmitters = emitters.get(memberId);

		if (userEmitters == null) {
			return;
		}

		userEmitters.remove(emitterId);

		if (userEmitters.isEmpty()) {
			emitters.remove(memberId);
		}
	}
}
