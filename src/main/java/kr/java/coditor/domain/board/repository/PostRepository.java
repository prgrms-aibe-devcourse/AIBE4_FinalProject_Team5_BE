package kr.java.coditor.domain.board.repository;

import kr.java.coditor.domain.board.dto.PostListResponse;
import kr.java.coditor.domain.board.dto.ProblemSimpleResponse;
import kr.java.coditor.domain.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

	// 1. 목록 조회
	@Query(value = "SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.problem",
		countQuery = "SELECT COUNT(p) FROM Post p")
	Page<Post> findAllWithUserAndProblem(Pageable pageable);

	@Query(value = "SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.problem " +
		"WHERE (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)",
		countQuery = "SELECT COUNT(p) FROM Post p " +
			"WHERE (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
	Page<Post> searchPostsWithUserAndProblem(@Param("keyword") String keyword, Pageable pageable);

	@Query(value = "SELECT new kr.java.coditor.domain.board.dto.PostListResponse(p.id, p.title, u.nickname, pr.id, p.createdAt) " +
		"FROM Post p " +
		"JOIN p.user u " +
		"LEFT JOIN p.problem pr " +
		"WHERE (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)"+
		"AND (:problemId IS NULL OR pr.id = :problemId)",
		countQuery = "SELECT COUNT(p) FROM Post p " +
			"LEFT JOIN p.problem pr " +
			"WHERE (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)"+
			"AND (:problemId IS NULL OR pr.id = :problemId)")
	Page<PostListResponse> searchPostsOptimized(@Param("keyword") String keyword, @Param("problemId") Long problemId, Pageable pageable);

	// 2. 단건 상세 조회
	@Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.problem WHERE p.id = :id")
	Optional<Post> findByIdWithUserAndProblem(@Param("id") Long id);

	@Query("SELECT DISTINCT new kr.java.coditor.domain.board.dto.ProblemSimpleResponse(pr.id, pr.title) " +
		"FROM Post p JOIN p.problem pr")
	List<ProblemSimpleResponse> findProblemsWithPosts();
}
