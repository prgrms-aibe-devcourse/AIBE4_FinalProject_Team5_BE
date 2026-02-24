package kr.java.coditor.domain.board.repository;

import kr.java.coditor.domain.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
