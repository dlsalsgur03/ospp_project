package com.oss_project.api.ranking.controller;

import com.oss_project.api.ranking.dto.RankingResponse;
import com.oss_project.api.ranking.service.RankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public List<RankingResponse> getRanking(
            @RequestParam(defaultValue = "TOTAL") String scope,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String department
    ) {
        return rankingService.getRanking(scope, college, department);
    }
}