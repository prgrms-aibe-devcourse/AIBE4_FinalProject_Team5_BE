package kr.java.coditor.domain.board.repository;

import kr.java.coditor.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
