package kr.java.coditor.domain.user.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ActivityDto {
    private LocalDate date;
    private Long count;

    public ActivityDto(LocalDate date, Long count) {
        this.date = date;
        this.count = count;
    }

    public ActivityDto(java.sql.Date date, Long count) {
        this.date = date.toLocalDate();
        this.count = count;
    }
}
