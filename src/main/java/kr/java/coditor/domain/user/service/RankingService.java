package kr.java.coditor.domain.user.service;

import kr.java.coditor.domain.grade.repository.SubmitRepository;
import kr.java.coditor.domain.user.dto.RankingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final SubmitRepository submitRepository;

    @Transactional(readOnly = true)
    public List<RankingDto> getRanking() {
        List<RankingDto> rankings = submitRepository.findRankingData();

        int currentRank = 1;
        for (int i = 0; i < rankings.size(); i++) {
            RankingDto dto = rankings.get(i);

            // 첫 번째 유저는 무조건 1등
            if (i == 0) {
                dto.setRank(currentRank);
            } else {
                RankingDto prevDto = rankings.get(i - 1);
                // 이전 유저와 푼 문제 개수가 같으면 공동 등수
                if (dto.getSolvedCount().equals(prevDto.getSolvedCount())) {
                    dto.setRank(prevDto.getRank());
                } else {
                    dto.setRank(i + 1);
                }
            }
        }
        return rankings;
    }
}
