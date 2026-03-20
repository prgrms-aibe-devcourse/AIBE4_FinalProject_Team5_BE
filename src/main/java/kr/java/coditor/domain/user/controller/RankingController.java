package kr.java.coditor.domain.user.controller;

import kr.java.coditor.domain.user.dto.RankingDto;
import kr.java.coditor.domain.user.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingDto>> getRanking() {
        return ResponseEntity.ok(rankingService.getRanking());
    }
}
