package com.oss_project.api.character.dto;

public record CharacterCatchRequest(
        Long userId,
        String characterCode,
        String characterName,
        String grade
) {
}