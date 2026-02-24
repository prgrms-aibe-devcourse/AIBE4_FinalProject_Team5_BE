package kr.java.coditor.domain.board.dto;

import kr.java.coditor.domain.board.entity.Post;
import java.time.LocalDateTime;

public record PostDetailResponse(
	Long id,
	String title,
	String content,
	String authorNickname,
	Long problemId,
	LocalDateTime createdAt
) {
	public static PostDetailResponse from(Post post) {
		return new PostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getUser().getNickname(),
			post.getProblem() != null ? post.getProblem().getId() : null,
			post.getCreatedAt()
		);
	}
}
