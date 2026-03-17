package kr.java.coditor.domain.board.repository;

import kr.java.coditor.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("SELECT c FROM Comment c JOIN FETCH c.user u LEFT JOIN FETCH u.userProfile WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
	List<Comment> findAllByPostIdWithUser(@Param("postId") Long postId);
}
