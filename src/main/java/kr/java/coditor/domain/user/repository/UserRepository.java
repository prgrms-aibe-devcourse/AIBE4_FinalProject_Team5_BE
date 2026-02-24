package kr.java.coditor.domain.user.repository;

import kr.java.coditor.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
