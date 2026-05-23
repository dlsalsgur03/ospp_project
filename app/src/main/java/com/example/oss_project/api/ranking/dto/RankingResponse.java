package com.oss_project.api.ranking.dto;

public record RankingResponse(
        int rank,
        Long userId,
        String nickname,
        String college,
        String department,
        int level,
        int exp
) {
}