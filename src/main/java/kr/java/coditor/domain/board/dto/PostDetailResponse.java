package kr.java.coditor.domain.board.dto;

import kr.java.coditor.domain.board.entity.Post;
import java.time.LocalDateTime;

public record PostDetailResponse(
	Long id,
	String title,
	String content,
	Long authorId,
	String authorNickname,
	Long problemId,
	String problemTitle,
	LocalDateTime createdAt
) {
	public static PostDetailResponse from(Post post) {
		return new PostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getUser().getId(),
			post.getUser().getNickname(),
			post.getProblem() != null ? post.getProblem().getId() : null,
			post.getProblem() != null ? post.getProblem().getTitle() : null,
			post.getCreatedAt()
		);
	}
}
