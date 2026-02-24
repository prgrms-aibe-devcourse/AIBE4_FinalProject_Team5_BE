package kr.java.coditor.domain.board.service;

import kr.java.coditor.domain.board.dto.PostCreateRequest;
import kr.java.coditor.domain.board.dto.PostDetailResponse;
import kr.java.coditor.domain.board.dto.PostListResponse;
import kr.java.coditor.domain.board.dto.PostResponse;
import kr.java.coditor.domain.board.entity.Post;
import kr.java.coditor.domain.board.exception.BoardException;
import kr.java.coditor.domain.board.exception.BoardErrorCode;
import kr.java.coditor.domain.board.repository.PostRepository;
import kr.java.coditor.domain.problem.entity.Problem;
import kr.java.coditor.domain.problem.repository.ProblemRepository;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final ProblemRepository problemRepository;
	private final UserRepository userRepository;

	@Transactional
	public PostResponse createPost(Long userId, PostCreateRequest request) {
		log.info("게시글 생성 요청 처리 시작 - userId: {}, title: {}", userId, request.title());

		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.error("게시글 생성 실패 - 존재하지 않는 유저 ID: {}", userId);
				return new BoardException(BoardErrorCode.USER_NOT_FOUND);
			});

		Problem problem = null;
		if (request.problemId() != null) {
			problem = problemRepository.findById(request.problemId())
				.orElseThrow(() -> {
					log.error("게시글 생성 실패 - 존재하지 않는 문제 ID: {}", request.problemId());
					return new BoardException(BoardErrorCode.PROBLEM_NOT_FOUND);
				});
			log.info("게시글에 연관된 문제 조회 성공 - problemId: {}", problem.getId());
		}

		Post post = Post.builder()
			.user(user)
			.problem(problem)
			.title(request.title())
			.content(request.content())
			.build();

		Post savedPost = postRepository.save(post);
		log.info("게시글 생성 완료 - postId: {}", savedPost.getId());

		return PostResponse.from(savedPost);
	}

	@Transactional(readOnly = true)
	public Page<PostListResponse> getPostList(Pageable pageable) {
		log.info("게시글 목록 조회 요청 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

		return postRepository.findAllWithUserAndProblem(pageable)
			.map(PostListResponse::from);
	}

	@Transactional(readOnly = true)
	public PostDetailResponse getPostDetail(Long postId) {
		log.info("게시글 상세 조회 요청 - postId: {}", postId);

		Post post = postRepository.findByIdWithUserAndProblem(postId)
			.orElseThrow(() -> {
				log.error("게시글 조회 실패 - 존재하지 않는 게시글 ID: {}", postId);
				return new BoardException(BoardErrorCode.POST_NOT_FOUND);
			});

		return PostDetailResponse.from(post);
	}
}
