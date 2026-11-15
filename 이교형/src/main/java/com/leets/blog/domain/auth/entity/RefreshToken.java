package com.leets.blog.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// 회원당 Refresh Token 1개 (로그인·재발급마다 새 값으로 교체)
// DB가 유출돼도 바로 쓸 수 없도록 원문 대신 SHA-256 해시만 저장
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    private RefreshToken(Long memberId, String token) {
        this.memberId = memberId;
        this.tokenHash = hash(token);
    }

    public static RefreshToken of(Long memberId, String token) {
        return new RefreshToken(memberId, token);
    }

    // 재발급·재로그인 시 새 토큰으로 교체 → 이전 토큰은 더 이상 사용 불가
    public void rotate(String token) {
        this.tokenHash = hash(token);
    }

    public boolean matches(String token) {
        return MessageDigest.isEqual(
                tokenHash.getBytes(StandardCharsets.US_ASCII),
                hash(token).getBytes(StandardCharsets.US_ASCII));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 지원하지 않는 JVM입니다.", e);
        }
    }
}
