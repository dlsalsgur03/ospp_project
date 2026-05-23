package com.oss_project.api.exp.dto;

public record ExpEventResponse(
        Long userId,
        int level,
        int exp,
        String message
) {
}