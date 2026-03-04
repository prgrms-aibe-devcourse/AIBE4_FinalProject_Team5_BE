package kr.java.coditor.domain.problem.repository;

import kr.java.coditor.domain.problem.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import kr.java.coditor.domain.problem.entity.Problem;
import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
	// 채점용 객체검색 인터페이스 한줄 수정입니다.
	List<TestCase> findByProblem(Problem problem);
}
