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

    /**
     * [확장성 설계] 계층형 대댓글(Self-Referencing) 구조를 대비한 자가 참조 매핑.
     * - 현 단계: 단방향 @ManyToOne 참조 필드만 선언 (최상위 댓글은 null).
     * - 추후 구현: 대댓글 계층형 재귀 조회 및 계층 구조 DTO 변환 로직 적용 예정.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public Comment(Member member, Post post, Comment parent, String content) {
        this.member = member;
        this.post = post;
        this.parent = parent;
        this.content = content;
    }
}