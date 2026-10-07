package com.leets.mission.domain.post;

import com.leets.mission.domain.member.Member;
import com.leets.mission.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    /**
     * [확장성 설계] 게시글 조회수 카운트 필드.
     * - 현 단계: 필드 선언 및 기본값(0) 설정.
     * - 추후 구현: 조회수 중복 증가 방지(Cookie/IP 기반), Redis 캐싱 및 동시성 제어 로직 적용 예정.
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Builder
    public Post(Member member, String title, String content, Integer viewCount) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount != null ? viewCount : 0;
    }
}