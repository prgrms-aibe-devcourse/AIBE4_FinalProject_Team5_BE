package kr.java.coditor.domain.grade.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.java.coditor.domain.grade.entity.GradeResult;

public interface GradeResultRepository extends JpaRepository<GradeResult, Long> {
}
