package kr.java.coditor.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

public class NotificationMessage implements Serializable {

	@JsonProperty("id")
	private String id;

	//JSON 키 값 강제 지정
	@JsonProperty("memberId")
	private Long memberId;

	@JsonProperty("message")
	private String message;

	@JsonProperty("targetUrl")
	private String targetUrl;

	@JsonProperty("isRead")
	private boolean isRead;

	@JsonProperty("createdAt")
	private Long createdAt;

	// Jackson 역직렬화용 기본 생성자
	public NotificationMessage() {
	}

	// 객체 생성자
	public NotificationMessage(Long memberId, String message, String targetUrl) {
		this.id = UUID.randomUUID().toString();
		this.memberId = memberId;
		this.message = message;
		this.targetUrl = targetUrl;
		this.isRead = false;
		this.createdAt = System.currentTimeMillis();
	}

	// --- Getters ---
	public String getId() { return id; }
	public Long getMemberId() { return memberId; }
	public String getMessage() { return message; }
	public String getTargetUrl() { return targetUrl; }
	public boolean isRead() { return isRead; }
	public Long getCreatedAt() { return createdAt; }

	// --- Setters ---
	public void setId(String id) { this.id = id; }
	public void setMemberId(Long memberId) { this.memberId = memberId; }
	public void setMessage(String message) { this.message = message; }
	public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
	public void setRead(boolean read) { this.isRead = read; }
	public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
