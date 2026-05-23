package com.oss_project.api.character.controller;

import com.oss_project.api.character.dto.CharacterCatchRequest;
import com.oss_project.api.character.dto.CharacterResponse;
import com.oss_project.api.character.service.CharacterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping("/catch")
    public CharacterResponse catchCharacter(@RequestBody CharacterCatchRequest request) {
        return characterService.catchCharacter(request);
    }

    @GetMapping("/users/{userId}")
    public List<CharacterResponse> getUserCharacters(@PathVariable Long userId) {
        return characterService.getUserCharacters(userId);
    }
}