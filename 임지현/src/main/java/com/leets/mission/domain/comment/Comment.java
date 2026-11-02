package com.leets.mission.domain.comment;

import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.post.Post;
import com.leets.mission.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status;

    @Builder
    public Comment(Member member, Post post, Comment parent, String content) {
        this.member = member;
        this.post = post;
        this.parent = parent;
        this.content = content;
        this.status = CommentStatus.ACTIVE;
    }

    public void hide() {
        if (this.status != CommentStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태의 댓글만 숨길 수 있습니다.");
        }
        this.status = CommentStatus.HIDDEN;
    }
}