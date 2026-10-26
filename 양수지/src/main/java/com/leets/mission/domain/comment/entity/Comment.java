package com.leets.mission.domain.comment.entity;

import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.user.entity.User;
import com.leets.mission.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 댓글 (setter 없이 의미 있는 메서드로만 상태를 변경한다)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    // 댓글이 달린 게시글 (댓글 N : 게시글 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 작성자 (댓글 N : 사용자 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 댓글 상태 (생성 시 ACTIVE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status;

    @Builder
    private Comment(String content, Post post, User user) {
        this.content = content;
        this.post = post;
        this.user = user;
        this.status = CommentStatus.ACTIVE;
    }

    // 숨김 상태인지 확인
    public boolean isHidden() {
        return status == CommentStatus.HIDDEN;
    }

    // 사용자가 작성한 댓글인지 확인
    public boolean isWrittenBy(Long userId) {
        return user.getId().equals(userId);
    }
}
