package kr.java.coditor.domain.board.dto;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
	Long problemId,

	@NotBlank(message = "제목은 필수입니다.")
	String title,

	@NotBlank(message = "내용은 필수입니다.")
	String content
) {
}
