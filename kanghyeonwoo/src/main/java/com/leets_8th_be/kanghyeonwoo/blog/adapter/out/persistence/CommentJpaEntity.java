package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
class CommentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private Visibility status = Visibility.ACTIVE;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostJpaEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentJpaEntity parent;

    @OneToMany(mappedBy = "parent")
    private List<CommentJpaEntity> children = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CommentJpaEntity() {
    }

    Long getId() { return id; }
    Visibility getStatus() { return status; }
    String getContent() { return content; }
    boolean isDeleted() { return isDeleted; }
    Long getUserId() { return userId; }
    Long getPostId() { return post == null ? null : post.getId(); }
    PostJpaEntity.Visibility getPostStatus() { return post == null ? null : post.getStatus(); }
    Long getParentId() { return parent == null ? null : parent.getId(); }
    LocalDateTime getCreatedAt() { return createdAt; }

    void hide() { status = Visibility.HIDDEN; }

    enum Visibility { ACTIVE, HIDDEN }
}
