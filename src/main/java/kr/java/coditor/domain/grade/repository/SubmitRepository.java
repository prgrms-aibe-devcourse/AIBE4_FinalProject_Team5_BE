package kr.java.coditor.domain.grade.repository;

import kr.java.coditor.domain.grade.entity.Submit;
import kr.java.coditor.domain.user.dto.ActivityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmitRepository extends JpaRepository<Submit, Long> {

	List<Submit> findByUserIdAndResult(Long userId, String result);

	@Query("SELECT new kr.java.coditor.domain.user.dto.ActivityDto(CAST(s.createdAt AS DATE), COUNT(s)) " +
		"FROM Submit s " +
		"WHERE s.userId = :userId AND s.result = 'CORRECT' " +
		"GROUP BY CAST(s.createdAt AS DATE)")
	List<ActivityDto> findUserActivity(@Param("userId") Long userId);
}
