package kr.java.coditor.domain.problem.repository;

import kr.java.coditor.domain.problem.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
}
