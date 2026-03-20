package kr.java.coditor.domain.grade.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "submits")
@Getter
public class Submit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "problem_id")
	private Long problemId;

	private String language;

	@Column(columnDefinition = "MEDIUMTEXT")
	private String code;

	private String result;

	@Column(name = "memory_usage")
	private Integer memoryUsage;

	@Column(name = "running_time")
	private Integer runningTime;

	@Column(name = "fail_reason", columnDefinition = "MEDIUMTEXT")
	private String failReason;

	@Column(name = "ai_review", columnDefinition = "TEXT")
	private String aiReview;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	protected Submit() {}

	public Submit(Long userId, Long problemId, String language, String code, String result, Integer memoryUsage, Integer runningTime, String failReason) {
		this.userId = userId;
		this.problemId = problemId;
		this.language = language;
		this.code = code;
		this.result = result;
		this.memoryUsage = memoryUsage;
		this.runningTime = runningTime;
		this.failReason = failReason;
		this.createdAt = LocalDateTime.now();
	}

	//ai리뷰 주입용
	public void setAiReview(String aiReview) {
		this.aiReview = aiReview;
	}
}
