package kr.java.coditor.domain.board.dto;

import kr.java.coditor.domain.board.entity.Post;
import java.time.LocalDateTime;

public record PostResponse(
	Long id,
	Long userId,
	Long problemId,
	String title,
	String content,
	LocalDateTime createdAt
) {
	public static PostResponse from(Post post) {
		return new PostResponse(
			post.getId(),
			post.getUser().getId(),
			post.getProblem() != null ? post.getProblem().getId() : null,
			post.getTitle(),
			post.getContent(),
			post.getCreatedAt()
		);
	}
}
