package kr.java.coditor.domain.board.dto;

import kr.java.coditor.domain.board.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
	Long id,
	Long postId,
	Long parentId,
	String content,
	Long userId,
	String authorNickname,
	LocalDateTime createdAt
) {
	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getPost().getId(),
			comment.getParent() != null ? comment.getParent().getId() : null,
			comment.getContent(),
			comment.getUser().getId(),
			comment.getUser().getNickname(),
			comment.getCreatedAt()
		);
	}
}
