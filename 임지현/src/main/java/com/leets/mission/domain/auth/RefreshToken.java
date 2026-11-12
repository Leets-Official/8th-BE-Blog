package com.leets.mission.domain.auth;

import com.leets.mission.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "refresh_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_token_member", columnNames = "member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public RefreshToken(Member member, String tokenHash, Instant expiresAt) {
        this.member = member;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public void rotate(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}