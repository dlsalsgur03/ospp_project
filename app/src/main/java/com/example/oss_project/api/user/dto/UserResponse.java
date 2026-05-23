package com.oss_project.api.user.dto;

public record UserResponse(
        Long userId,
        String email,
        String nickname,
        String college,
        String department,
        int level,
        int exp
) {
}