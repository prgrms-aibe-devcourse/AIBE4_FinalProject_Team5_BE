package kr.java.coditor.domain.notification.controller;

import kr.java.coditor.domain.notification.dto.NotificationMessage;
import kr.java.coditor.domain.notification.dto.NotificationPublishRequest;
import kr.java.coditor.domain.notification.service.NotificationService;
import kr.java.coditor.domain.notification.service.RedisPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final RedisPublisher redisPublisher;

	public NotificationController(
		NotificationService notificationService,
		RedisPublisher redisPublisher
	) {
		this.notificationService = notificationService;
		this.redisPublisher = redisPublisher;
	}

	@GetMapping(value = "/subscribe/{memberId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@PathVariable Long memberId) {
		return notificationService.subscribe(memberId);
	}

	@GetMapping("/{memberId}")
	public List<NotificationMessage> getNotifications(@PathVariable Long memberId) {
		return notificationService.getNotifications(memberId);
	}

	@PatchMapping("/{memberId}/{notificationId}/read")
	public ResponseEntity<Void> markAsRead(
		@PathVariable Long memberId,
		@PathVariable String notificationId
	) {
		boolean updated = notificationService.markAsRead(memberId, notificationId);

		if (!updated) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{memberId}/read-all")
	public ResponseEntity<Void> markAllAsRead(@PathVariable Long memberId) {
		notificationService.markAllAsRead(memberId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/test")
	public ResponseEntity<Void> publishTest(@RequestBody NotificationPublishRequest request) {
		NotificationMessage message = new NotificationMessage(
			request.getMemberId(),
			request.getMessage(),
			request.getTargetUrl()
		);

		redisPublisher.publish(message);
		return ResponseEntity.ok().build();
	}
}
