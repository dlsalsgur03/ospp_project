package com.oss_project.api.exp.dto;

public record ExpEventRequest(
        Long userId,
        String eventType,
        int amount
) {
}