package kr.java.coditor.domain.board.service;

import kr.java.coditor.domain.board.dto.*;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final ProblemRepository problemRepository;
	private final UserRepository userRepository;

	@Transactional
	public PostResponse createPost(String email, PostCreateRequest request) {
		log.info("게시글 생성 요청 처리 시작 - email: {}, title: {}", email, request.title());

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.error("게시글 생성 실패 - 존재하지 않는 유저 email: {}", email);
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
	public Page<PostListResponse> getPostList(String keyword, Long problemId, Pageable pageable) {
		log.info("게시글 목록 조회 요청 - keyword: {}, problemId: {}, page: {}, size: {}", keyword, problemId, pageable.getPageNumber(), pageable.getPageSize());

		return postRepository.searchPostsOptimized(keyword, problemId, pageable);
			//.map(PostListResponse::from);
	}

	@Transactional(readOnly = true)
	public List<ProblemSimpleResponse> getProblemsWithPosts() {
		log.info("질문이 달린 문제 목록 조회 요청");
		return postRepository.findProblemsWithPosts();
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

	@Transactional
	public PostResponse updatePost(String email, Long postId, PostUpdateRequest request) {
		log.info("게시글 수정 요청 - email: {}, postId: {}", email, postId);

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

		if (!post.getUser().getEmail().equals(email)) {
			log.warn("게시글 수정 권한 없음 - 요청 email: {}, 실제 작성자 email: {}", email, post.getUser().getEmail());
			throw new BoardException(BoardErrorCode.UNAUTHORIZED_ACTION);
		}

		post.update(request.title(), request.content());

		log.info("게시글 수정 완료 - postId: {}", postId);
		return PostResponse.from(post);
	}

	@Transactional
	public void deletePost(String email, Long postId) {
		log.info("게시글 삭제 요청 - email: {}, postId: {}", email, postId);

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

		if (!post.getUser().getEmail().equals(email)) {
			log.warn("게시글 삭제 권한 없음 - 요청 email: {}, 실제 작성자 email: {}", email, post.getUser().getEmail());
			throw new BoardException(BoardErrorCode.UNAUTHORIZED_ACTION);
		}

		postRepository.delete(post);
		log.info("게시글 삭제 완료 - postId: {}", postId);
	}

}
