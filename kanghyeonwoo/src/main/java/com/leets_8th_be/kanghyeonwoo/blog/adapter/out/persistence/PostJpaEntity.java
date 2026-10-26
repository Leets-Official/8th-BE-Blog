package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.search.domain.SearchNormalization;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentJpaEntity> comments = new ArrayList<>();

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

    void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    void hide() {
        status = Visibility.HIDDEN;
    }

    enum Visibility { ACTIVE, HIDDEN }
}
