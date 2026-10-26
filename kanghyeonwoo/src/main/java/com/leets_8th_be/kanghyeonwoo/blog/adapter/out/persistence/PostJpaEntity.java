package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.util.SearchNormalization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
class PostJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private Visibility status = Visibility.ACTIVE;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "normalized_title", length = 450)
    private String normalizedTitle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PostJpaEntity() {
    }

    private PostJpaEntity(String title, String content, Long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
    }

    static PostJpaEntity create(String title, String content, Long userId) {
        return new PostJpaEntity(title, content, userId);
    }

    @PrePersist
    void prepareInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeTitle();
    }

    @PreUpdate
    void prepareUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeTitle();
    }

    private void normalizeTitle() {
        normalizedTitle = SearchNormalization.normalize(title);
    }

    Long getId() { return id; }
    Visibility getStatus() { return status; }
    String getTitle() { return title; }
    String getContent() { return content; }
    Long getUserId() { return userId; }
    LocalDateTime getCreatedAt() { return createdAt; }
    LocalDateTime getUpdatedAt() { return updatedAt; }

    enum Visibility { ACTIVE, HIDDEN }
}
