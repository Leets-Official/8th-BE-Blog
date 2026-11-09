package com.example.demo.security;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String ISSUER = "leets-blog";
    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey key;
    private final long accessValiditySeconds;
    private final long refreshValiditySeconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-validity-seconds}") long accessValiditySeconds,
                            @Value("${jwt.refresh-token-validity-seconds}") long refreshValiditySeconds) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessValiditySeconds = accessValiditySeconds;
        this.refreshValiditySeconds = refreshValiditySeconds;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, ACCESS, accessValiditySeconds);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, REFRESH, refreshValiditySeconds);
    }

    public Long getMemberIdFromAccessToken(String token) {
        return parseMemberId(token, ACCESS);
    }

    public Long getMemberIdFromRefreshToken(String token) {
        return parseMemberId(token, REFRESH);
    }

    private String createToken(Long memberId, String type, long validitySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(memberId))
                .issuer(ISSUER)
                .claim(TOKEN_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(validitySeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Long parseMemberId(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .require(TOKEN_TYPE, expectedType)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}