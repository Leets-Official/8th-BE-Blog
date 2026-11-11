package com.leets.choyeji.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @Column(length = 64, nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Instant expiresAt;

    public RefreshToken(String tokenHash, Long memberId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.memberId = memberId;
        this.expiresAt = expiresAt;
    }
}
