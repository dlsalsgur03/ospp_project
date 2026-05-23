package com.oss_project.api.character.service;

import com.oss_project.api.character.domain.UserCharacter;
import com.oss_project.api.character.dto.CharacterCatchRequest;
import com.oss_project.api.character.dto.CharacterResponse;
import com.oss_project.api.character.repository.UserCharacterRepository;
import com.oss_project.api.exp.service.LevelEngine;
import com.oss_project.api.user.domain.User;
import com.oss_project.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CharacterService {

    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final LevelEngine levelEngine;

    public CharacterService(
            UserRepository userRepository,
            UserCharacterRepository userCharacterRepository,
            LevelEngine levelEngine
    ) {
        this.userRepository = userRepository;
        this.userCharacterRepository = userCharacterRepository;
        this.levelEngine = levelEngine;
    }

    @Transactional
    public CharacterResponse catchCharacter(CharacterCatchRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserCharacter character = new UserCharacter(
                user,
                request.characterCode(),
                request.characterName(),
                request.grade()
        );

        UserCharacter savedCharacter = userCharacterRepository.save(character);

        // 캐릭터 획득 시 경험치 30 지급
        levelEngine.applyExp(user, 30);

        return toResponse(savedCharacter);
    }

    @Transactional(readOnly = true)
    public List<CharacterResponse> getUserCharacters(Long userId) {
        return userCharacterRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CharacterResponse toResponse(UserCharacter character) {
        return new CharacterResponse(
                character.getId(),
                character.getCharacterCode(),
                character.getCharacterName(),
                character.getGrade(),
                character.getAcquiredAt()
        );
    }
}