package kr.java.coditor.domain.board.dto;

import kr.java.coditor.domain.board.entity.Post;
import java.time.LocalDateTime;

public record PostListResponse(
	Long id,
	String title,
	String authorNickname,
	Long problemId,
	LocalDateTime createdAt
) {
	public static PostListResponse from(Post post) {
		return new PostListResponse(
			post.getId(),
			post.getTitle(),
			post.getUser().getNickname(),
			post.getProblem() != null ? post.getProblem().getId() : null,
			post.getCreatedAt()
		);
	}
}
