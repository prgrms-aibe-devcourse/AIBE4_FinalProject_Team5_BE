package kr.java.coditor.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

	@OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProblemExample> examples = new ArrayList<>();

	public void addExample(ProblemExample example) {
		this.examples.add(example);
	}

	public void updateExamples(List<ProblemExample> newExamples) {
		this.examples.clear();
		if (newExamples != null) {
			this.examples.addAll(newExamples);
		}
	}

	@OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProblemTag> problemTags = new ArrayList<>();

	public void addProblemTag(ProblemTag problemTag) {
		this.problemTags.add(problemTag);
	}

	public void updateProblemTags(List<ProblemTag> newProblemTags) {
		this.problemTags.clear();
		if (newProblemTags != null) {
			this.problemTags.addAll(newProblemTags);
		}
	}

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

	/**
	 * 문제 부분 수정
	 */
	public void update(String title, String content, String inputDesc, String outputDesc,
					   Integer level, Double timeLimit, Integer memoryLimit, Boolean isVisible) {
		if (title != null) this.title = title;
		if (content != null) this.content = content;
		if (inputDesc != null) this.inputDesc = inputDesc;
		if (outputDesc != null) this.outputDesc = outputDesc;
		if (level != null) this.level = level;
		if (timeLimit != null) this.timeLimit = timeLimit;
		if (memoryLimit != null) this.memoryLimit = memoryLimit;
		if (isVisible != null) this.isVisible = isVisible;
	}
}
