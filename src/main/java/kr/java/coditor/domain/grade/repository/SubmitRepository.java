package kr.java.coditor.domain.grade.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.java.coditor.domain.grade.entity.Submit;

public interface SubmitRepository extends JpaRepository<Submit, Long> {
}
