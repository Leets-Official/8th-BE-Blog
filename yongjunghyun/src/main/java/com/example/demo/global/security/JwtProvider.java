package com.example.demo.global.security;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

// JWT를 만들고(발급) 검사(검증)하는 역할만 맡는다.
// 구조: Header.Payload.Signature — Payload는 Base64라 누구나 읽을 수 있으므로 비밀번호 같은 비밀은 넣지 않는다.
@Component
public class JwtProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
                       @Value("${jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds) {
        // HS256 서명에는 32바이트 이상의 키가 필요하다 (짧으면 여기서 예외가 나서 서버가 안 뜬다)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, ACCESS, accessTokenValiditySeconds);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, REFRESH, refreshTokenValiditySeconds);
    }

    private String createToken(Long memberId, String type, long validitySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))          // sub: 이 토큰의 주인(회원 ID)
                .claim(TYPE_CLAIM, type)                    // access인지 refresh인지 구분
                .id(UUID.randomUUID().toString())           // jti: 같은 초에 발급해도 토큰이 서로 다르게
                .issuedAt(Date.from(now))                   // iat: 발급 시각
                .expiration(Date.from(now.plusSeconds(validitySeconds))) // exp: 만료 시각
                .signWith(key)                              // 서버만 아는 키로 서명
                .compact();
    }

    // 액세스 토큰을 검증하고 회원 ID를 꺼낸다. (필터에서 사용)
    public Long getMemberIdFromAccessToken(String token) {
        return getMemberId(parse(token, ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN), ACCESS, ErrorCode.INVALID_TOKEN);
    }

    // 리프레시 토큰을 검증하고 회원 ID를 꺼낸다. (재발급에서 사용)
    public Long getMemberIdFromRefreshToken(String token) {
        return getMemberId(parse(token, ErrorCode.INVALID_REFRESH_TOKEN, ErrorCode.INVALID_REFRESH_TOKEN), REFRESH, ErrorCode.INVALID_REFRESH_TOKEN);
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(refreshTokenValiditySeconds);
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    // "디코딩"이 아니라 "검증": 서명이 맞는지, 만료되지 않았는지 확인한 뒤에만 내용을 믿는다
    private Claims parse(String token, ErrorCode invalid, ErrorCode expired) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(expired);
        } catch (JwtException | IllegalArgumentException e) {   // 서명 위조, 형식 오류, 빈 문자열 등
            throw new CustomException(invalid);
        }
    }

    private Long getMemberId(Claims claims, String expectedType, ErrorCode errorCode) {
        if (!expectedType.equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new CustomException(errorCode);   // 리프레시 토큰으로 API를 부르는 것 등을 막는다
        }
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new CustomException(errorCode);
        }
    }
}
