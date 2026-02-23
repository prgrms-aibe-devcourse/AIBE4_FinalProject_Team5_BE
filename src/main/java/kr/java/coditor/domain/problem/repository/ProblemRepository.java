package kr.java.coditor.domain.problem.repository;

import kr.java.coditor.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
}
