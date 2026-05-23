package com.oss_project.api.character.domain;

import com.oss_project.api.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_characters")
public class UserCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String characterCode;

    private String characterName;

    private String grade;

    private LocalDateTime acquiredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected UserCharacter() {
    }

    public UserCharacter(User user, String characterCode, String characterName, String grade) {
        this.user = user;
        this.characterCode = characterCode;
        this.characterName = characterName;
        this.grade = grade;
        this.acquiredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCharacterCode() {
        return characterCode;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getGrade() {
        return grade;
    }

    public LocalDateTime getAcquiredAt() {
        return acquiredAt;
    }

    public User getUser() {
        return user;
    }
}