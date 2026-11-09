package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 회원당 Refresh Token 1개만 보관한다. (원문이 아니라 SHA-256 해시만 저장)
// 재발급할 때마다 해시를 교체(회전)하므로, 이미 사용한 토큰은 해시가 달라져 재사용을 감지할 수 있다.
@Getter
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    public RefreshToken(Long userId, String tokenHash) {
        this.userId = userId;
        this.tokenHash = tokenHash;
    }

    public void rotate(String newTokenHash) {
        this.tokenHash = newTokenHash;
    }

    public boolean matches(String tokenHash) {
        return this.tokenHash.equals(tokenHash);
    }
}
