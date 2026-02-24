package kr.java.coditor.domain.board.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentReadResponse(
	Long id,
	String content,
	String authorNickname,
	LocalDateTime createdAt,
	List<CommentReadResponse> children
) {
}
