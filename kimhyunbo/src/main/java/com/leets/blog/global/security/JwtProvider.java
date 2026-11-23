package com.leets.blog.global.security;

import com.leets.blog.global.exception.CustomException;
import com.leets.blog.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE = "type";

    private enum TokenType { ACCESS, REFRESH }

    private final SecretKey key;

    @Getter
    private final Duration accessTokenValidity;

    @Getter
    private final Duration refreshTokenValidity;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-validity}") Duration accessTokenValidity,
                       @Value("${jwt.refresh-token-validity}") Duration refreshTokenValidity) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, TokenType.ACCESS, accessTokenValidity);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, TokenType.REFRESH, refreshTokenValidity);
    }

    // Access Token이 맞는지 검증하고 회원 ID를 꺼낸다.
    public Long getMemberIdFromAccessToken(String token) {
        return parse(token, TokenType.ACCESS);
    }

    // Refresh Token이 맞는지 검증하고 회원 ID를 꺼낸다.
    public Long getMemberIdFromRefreshToken(String token) {
        return parse(token, TokenType.REFRESH);
    }

    private String createToken(Long memberId, TokenType type, Duration validity) {
        Date now = new Date();
        return Jwts.builder()
                // 같은 초에 여러 번 발급해도 토큰이 겹치지 않도록 고유 ID를 넣는다.
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(memberId))
                .claim(TOKEN_TYPE, type.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity.toMillis()))
                .signWith(key)
                .compact();
    }

    private Long parse(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            // Refresh Token으로 API를 호출하거나, Access Token으로 재발급을 받는 것을 막는다.
            if (!expectedType.name().equals(claims.get(TOKEN_TYPE, String.class))) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
