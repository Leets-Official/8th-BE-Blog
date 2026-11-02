package com.leets.mission.domain.post;

import com.leets.mission.domain.member.Member;
import com.leets.mission.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status;

    @Builder
    public Post(
            Member member,
            String title,
            String content,
            Integer viewCount,
            PostStatus status
    ) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.status = PostStatus.ACTIVE;
    }
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void hide() {
        validateActive();
        this.status = PostStatus.HIDDEN;
    }

    private void validateActive() {
        if (this.status != PostStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태의 게시글만 숨길 수 있습니다.");
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}