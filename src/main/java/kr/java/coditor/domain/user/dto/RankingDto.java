package kr.java.coditor.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingDto {
    private Long userId;
    private String nickname;
    private String introduce;
    private Long solvedCount;
    private Integer rank;
}
