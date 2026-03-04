package kr.java.coditor.domain.notification.dto;

public class NotificationMessage {

	private Long memberId;
	private String message;

	public NotificationMessage() {}

	public NotificationMessage(Long memberId, String message) {
		this.memberId = memberId;
		this.message = message;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getMessage() {
		return message;
	}
}
