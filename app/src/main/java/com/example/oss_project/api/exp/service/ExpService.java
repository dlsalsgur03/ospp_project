package com.oss_project.api.exp.service;

import com.oss_project.api.exp.dto.ExpEventRequest;
import com.oss_project.api.exp.dto.ExpEventResponse;
import com.oss_project.api.user.domain.User;
import com.oss_project.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpService {

    private final UserRepository userRepository;
    private final LevelEngine levelEngine;

    public ExpService(UserRepository userRepository, LevelEngine levelEngine) {
        this.userRepository = userRepository;
        this.levelEngine = levelEngine;
    }

    @Transactional
    public ExpEventResponse addExp(ExpEventRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.amount() <= 0) {
            throw new IllegalArgumentException("경험치는 1 이상이어야 합니다.");
        }

        levelEngine.applyExp(user, request.amount());

        return new ExpEventResponse(
                user.getId(),
                user.getLevel(),
                user.getExp(),
                "경험치가 지급되었습니다."
        );
    }
}