package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
            @jakarta.persistence.UniqueConstraint(name = "uk_user_email", columnNames = "email"),
            @jakarta.persistence.UniqueConstraint(
                    name = "uk_user_nickname",
                    columnNames = "username")
        })
class UserProfileJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private String passwordHash;

    @Column(unique = true)
    private String kakaoId;

    @Column(nullable = false, columnDefinition = "varchar(16) default 'USER'")
    private String role = "USER";

    void credentials(String hash, String kakao) {
        passwordHash = hash;
        kakaoId = kakao;
    }

    String passwordHash() {
        return passwordHash;
    }

    String kakaoId() {
        return kakaoId;
    }

    String role() {
        return role;
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserProfileJpaEntity() {}

    UserProfileJpaEntity(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @PrePersist
    void createTimestamps() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    Long getId() {
        return id;
    }

    String getUsername() {
        return username;
    }

    String getEmail() {
        return email;
    }
}
