package com.leets.blog.domain.member.entity;

import com.leets.blog.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 소셜 계정(카카오 회원번호)과 우리 서비스 회원의 연결
// Member에 컬럼을 추가하지 않고 테이블을 나눠, 다른 소셜 로그인이 늘어나도 Member는 그대로 둠
@Entity
@Table(name = "social_accounts", uniqueConstraints =
        @UniqueConstraint(name = "uk_social_accounts_provider_id", columnNames = {"provider", "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    // 카카오 회원번호 (카카오 계정마다 고정된 값)
    @Column(name = "provider_id", nullable = false, length = 64)
    private String providerId;

    private SocialAccount(Member member, SocialProvider provider, String providerId) {
        this.member = member;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static SocialAccount of(Member member, SocialProvider provider, String providerId) {
        return new SocialAccount(member, provider, providerId);
    }
}
