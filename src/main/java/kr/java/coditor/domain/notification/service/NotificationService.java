package kr.java.coditor.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter subscribe(Long memberId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitters.put(memberId, emitter);

		emitter.onCompletion(() -> emitters.remove(memberId));
		emitter.onTimeout(() -> emitters.remove(memberId));
		emitter.onError(e -> emitters.remove(memberId));

		try {
			// 연결 성공 시 더미 데이터 전송 (503 에러 방지)
			emitter.send(SseEmitter.event().name("connect").data("connected!"));
		} catch (IOException e) {
			emitters.remove(memberId);
			log.error("SSE 연결 초기화 오류. memberId: {}", memberId, e);
		}

		return emitter;
	}

	public void sendToClient(Long memberId, String message) {
		SseEmitter emitter = emitters.get(memberId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("grading-result").data(message));
			} catch (IOException e) {
				emitters.remove(memberId);
				log.error("SSE 메시지 전송 실패. memberId: {}", memberId, e);
			}
		}
	}
}
