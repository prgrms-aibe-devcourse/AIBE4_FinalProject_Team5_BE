package kr.java.coditor.domain.board.service;

import kr.java.coditor.domain.board.dto.CommentCreateRequest;
import kr.java.coditor.domain.board.dto.CommentResponse;
import kr.java.coditor.domain.board.entity.Comment;
import kr.java.coditor.domain.board.entity.Post;
import kr.java.coditor.domain.board.exception.BoardErrorCode;
import kr.java.coditor.domain.board.exception.BoardException;
import kr.java.coditor.domain.board.repository.CommentRepository;
import kr.java.coditor.domain.board.repository.PostRepository;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	@Transactional
	public CommentResponse createComment(Long userId, CommentCreateRequest request) {
		log.info("댓글 등록 요청 - userId: {}, postId: {}", userId, request.postId());

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BoardException(BoardErrorCode.USER_NOT_FOUND));

		Post post = postRepository.findById(request.postId())
			.orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

		Comment parent = null;
		if (request.parentId() != null) {
			parent = commentRepository.findById(request.parentId())
				.orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

			if (!parent.getPost().getId().equals(post.getId())) {
				log.warn("대댓글 작성 실패 - 부모 댓글의 postId와 현재 postId가 불일치");
				throw new BoardException(BoardErrorCode.INVALID_PARENT_COMMENT);
			}
		}

		Comment comment = Comment.builder()
			.post(post)
			.user(user)
			.parent(parent)
			.content(request.content())
			.build();

		Comment savedComment = commentRepository.save(comment);
		log.info("댓글 등록 완료 - commentId: {}", savedComment.getId());

		return CommentResponse.from(savedComment);
	}
}
