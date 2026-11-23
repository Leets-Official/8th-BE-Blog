package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.jwt;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.VerifiedToken;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JjwtTokenAdapter implements TokenProvider {
    private static final String ISSUER = "leets-blog";
    private final SecretKey key;

    public JjwtTokenAdapter(@Value("${auth.jwt-secret:}") String secret) {
        key = secret.isBlank()
                ? Jwts.SIG.HS256.key().build()
                : Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    @Override
    public String issue(long userId, TokenType type, String sessionId, Duration lifetime) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(Long.toString(userId))
                .id(UUID.randomUUID().toString())
                .claim("type", type.claim())
                .claim("sid", sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(key)
                .compact();
    }

    @Override
    public VerifiedToken verify(String token, TokenType type) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .require("type", type.claim())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getExpiration() == null
                    || claims.getSubject() == null
                    || claims.get("sid", String.class) == null) {
                throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
            }
            return new VerifiedToken(Long.parseLong(claims.getSubject()), claims.get("sid", String.class));
        } catch (JwtException | IllegalArgumentException failure) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
    }
}
