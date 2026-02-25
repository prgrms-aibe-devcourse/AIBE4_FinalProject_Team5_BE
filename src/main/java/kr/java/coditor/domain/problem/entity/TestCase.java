package kr.java.coditor.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestCase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id", nullable = false)
	private Problem problem;

	@Column(name = "input_url", columnDefinition = "TEXT")
	private String inputUrl;

	@Column(name = "output_url", columnDefinition = "TEXT")
	private String outputUrl;

	@Builder
	public TestCase(Problem problem, String inputUrl, String outputUrl) {
		this.problem = problem;
		this.inputUrl = inputUrl;
		this.outputUrl = outputUrl;
	}

	public void updateUrls(String inputUrl, String outputUrl) {
		this.inputUrl = inputUrl;
		this.outputUrl = outputUrl;
	}
}
