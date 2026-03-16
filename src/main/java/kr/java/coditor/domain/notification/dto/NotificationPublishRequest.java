package kr.java.coditor.domain.notification.dto;

public class NotificationPublishRequest {

	private Long memberId;
	private String message;
	private String targetUrl;

	public NotificationPublishRequest() {
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getMessage() {
		return message;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
}
