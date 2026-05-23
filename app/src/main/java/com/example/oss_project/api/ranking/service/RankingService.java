package com.oss_project.api.ranking.service;

import com.oss_project.api.ranking.dto.RankingResponse;
import com.oss_project.api.user.domain.User;
import com.oss_project.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RankingService {

    private final UserRepository userRepository;

    public RankingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingResponse> getRanking(String scope, String college, String department) {
        List<User> users;

        if ("COLLEGE".equalsIgnoreCase(scope)) {
            if (college == null || college.isBlank()) {
                throw new IllegalArgumentException("college 값이 필요합니다.");
            }
            users = userRepository.findByCollegeOrderByLevelDescExpDesc(college);
        } else if ("DEPARTMENT".equalsIgnoreCase(scope)) {
            if (department == null || department.isBlank()) {
                throw new IllegalArgumentException("department 값이 필요합니다.");
            }
            users = userRepository.findByDepartmentOrderByLevelDescExpDesc(department);
        } else {
            users = userRepository.findAllByOrderByLevelDescExpDesc();
        }

        AtomicInteger rank = new AtomicInteger(1);

        return users.stream()
                .map(user -> new RankingResponse(
                        rank.getAndIncrement(),
                        user.getId(),
                        user.getNickname(),
                        user.getCollege(),
                        user.getDepartment(),
                        user.getLevel(),
                        user.getExp()
                ))
                .toList();
    }
}