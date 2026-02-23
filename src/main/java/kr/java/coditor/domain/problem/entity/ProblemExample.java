package kr.java.coditor.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "problem_examples")
public class ProblemExample {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id", nullable = false)
	private Problem problem;

	@Column(name = "input_example", columnDefinition = "TEXT")
	private String inputExample;

	@Column(name = "output_example", columnDefinition = "TEXT")
	private String outputExample;

	@Builder
	public ProblemExample(Problem problem, String inputExample, String outputExample) {
		this.problem = problem;
		this.inputExample = inputExample;
		this.outputExample = outputExample;
	}
}
