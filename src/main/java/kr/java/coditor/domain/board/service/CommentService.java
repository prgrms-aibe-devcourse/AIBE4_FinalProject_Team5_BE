package kr.java.coditor.domain.board.service;

import kr.java.coditor.domain.board.dto.CommentCreateRequest;
import kr.java.coditor.domain.board.dto.CommentReadResponse;
import kr.java.coditor.domain.board.dto.CommentResponse;
import kr.java.coditor.domain.board.dto.CommentUpdateRequest;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Transactional(readOnly = true)
	public List<CommentReadResponse> getComments(Long postId) {
		log.info("게시글 댓글 목록 조회 요청 - postId: {}", postId);

		if (!postRepository.existsById(postId)) {
			throw new BoardException(BoardErrorCode.POST_NOT_FOUND);
		}

		List<Comment> comments = commentRepository.findAllByPostIdWithUser(postId);

		Map<Long, CommentReadResponse> map = new HashMap<>();
		List<CommentReadResponse> roots = new ArrayList<>();

		for (Comment c : comments) {
			map.put(c.getId(), new CommentReadResponse(
				c.getId(),
				c.getContent(),
				c.getUser().getId(),
				c.getUser().getNickname(),
				c.getCreatedAt(),
				new ArrayList<>()
			));
		}

		for (Comment c : comments) {
			CommentReadResponse dto = map.get(c.getId());

			if (c.getParent() != null) {
				CommentReadResponse parentDto = map.get(c.getParent().getId());
				if (parentDto != null) {
					parentDto.children().add(dto);
				}
			} else {
				roots.add(dto);
			}
		}

		return roots;
	}

	@Transactional
	public CommentResponse updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
		log.info("댓글 수정 요청 - userId: {}, commentId: {}", userId, commentId);

		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

		if (!comment.getUser().getId().equals(userId)) {
			log.warn("댓글 수정 권한 없음 - 요청 userId: {}, 실제 작성자 ID: {}", userId, comment.getUser().getId());
			throw new BoardException(BoardErrorCode.UNAUTHORIZED_ACTION);
		}

		comment.updateContent(request.content());

		log.info("댓글 수정 완료 - commentId: {}", commentId);
		return CommentResponse.from(comment);
	}

	@Transactional
	public void deleteComment(Long userId, Long commentId) {
		log.info("댓글 삭제 요청 - userId: {}, commentId: {}", userId, commentId);

		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

		if (!comment.getUser().getId().equals(userId)) {
			log.warn("댓글 삭제 권한 없음 - 요청 userId: {}, 실제 작성자 ID: {}", userId, comment.getUser().getId());
			throw new BoardException(BoardErrorCode.UNAUTHORIZED_ACTION);
		}

		commentRepository.delete(comment);
		log.info("댓글 삭제 완료 - commentId: {}", commentId);
	}

}
