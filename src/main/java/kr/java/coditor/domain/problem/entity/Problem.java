package kr.java.coditor.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "problems")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "MEDIUMTEXT")
	private String content;

	@Column(name = "input_desc", columnDefinition = "TEXT")
	private String inputDesc;

	@Column(name = "output_desc", columnDefinition = "TEXT")
	private String outputDesc;

	private Integer level;

	@Column(name = "time_limit")
	private Double timeLimit;

	@Column(name = "memory_limit")
	private Integer memoryLimit;

	@Column(name = "is_visible")
	private Boolean isVisible;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Problem(String title, String content, String inputDesc, String outputDesc,
				   Integer level, Double timeLimit, Integer memoryLimit, Boolean isVisible) {
		this.title = title;
		this.content = content;
		this.inputDesc = inputDesc;
		this.outputDesc = outputDesc;
		this.level = level;
		this.timeLimit = timeLimit;
		this.memoryLimit = memoryLimit;
		this.isVisible = isVisible;
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
