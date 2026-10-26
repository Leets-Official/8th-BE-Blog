package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
class UserProfileJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserProfileJpaEntity() {
    }

    UserProfileJpaEntity(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @PrePersist
    void createTimestamps() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void updateTimestamp() { updatedAt = LocalDateTime.now(); }

    Long getId() { return id; }
    String getUsername() { return username; }
    String getEmail() { return email; }
}
