package com.oss_project.api.user.dto;

public record SignupRequest(
        String email,
        String password,
        String nickname,
        String college,
        String department
) {
}