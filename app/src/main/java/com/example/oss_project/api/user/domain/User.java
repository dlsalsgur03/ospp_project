package com.oss_project.api.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String nickname;

    private String college;

    private String department;

    private int level;

    private int exp;

    private LocalDateTime createdAt;

    protected User() {
    }

    public User(String email, String password, String nickname, String college, String department) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.college = college;
        this.department = department;
        this.level = 1;
        this.exp = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCollege() {
        return college;
    }

    public String getDepartment() {
        return department;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void addExp(int amount) {
        this.exp += amount;
    }

    public void decreaseExp(int amount) {
        this.exp -= amount;
    }

    public void levelUp() {
        this.level += 1;
    }
}