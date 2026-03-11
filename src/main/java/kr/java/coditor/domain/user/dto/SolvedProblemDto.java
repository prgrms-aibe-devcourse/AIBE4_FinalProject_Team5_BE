package kr.java.coditor.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SolvedProblemDto {
    private Long id;
    private String title;
    private Integer level;
    private LocalDate solvedAt;
    private String language;
}
