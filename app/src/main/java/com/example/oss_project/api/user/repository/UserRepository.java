package com.oss_project.api.user.repository;

import com.oss_project.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    List<User> findAllByOrderByLevelDescExpDesc();

    List<User> findByCollegeOrderByLevelDescExpDesc(String college);

    List<User> findByDepartmentOrderByLevelDescExpDesc(String department);
}