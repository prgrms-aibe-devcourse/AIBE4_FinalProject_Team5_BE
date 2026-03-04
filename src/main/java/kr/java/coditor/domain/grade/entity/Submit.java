package kr.java.coditor.domain.grade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "submits")
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
}
