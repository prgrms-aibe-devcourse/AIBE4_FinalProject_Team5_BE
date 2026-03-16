package kr.java.coditor.domain.problem.repository;

import kr.java.coditor.domain.problem.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findAllByIdIn(List<Long> ids);

	@Query(value = "SELECT DISTINCT p FROM Problem p " +
		"LEFT JOIN p.problemTags pt " +
		"LEFT JOIN pt.tag t " +
		"WHERE (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) " +
		"AND (:level IS NULL OR p.level = :level) " +
		"AND (:tag IS NULL OR :tag = '' OR t.name = :tag)",
		countQuery = "SELECT count(DISTINCT p) FROM Problem p " +
			"LEFT JOIN p.problemTags pt " +
			"LEFT JOIN pt.tag t " +
			"WHERE (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) " +
			"AND (:level IS NULL OR p.level = :level) " +
			"AND (:tag IS NULL OR :tag = '' OR t.name = :tag)")
	Page<Problem> searchProblems(@Param("keyword") String keyword,
								 @Param("level") Integer level,
								 @Param("tag") String tag,
								 Pageable pageable);
}
