package com.leets_8th_be.kanghyeonwoo.auth;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

import javax.crypto.SecretKey;

@Component
public class JwtTokens
        implements com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Tokens {
    private final SecretKey key;

    public JwtTokens(@Value("${auth.jwt-secret:}") String secret) {
        if (secret.isBlank()) {
            key = Jwts.SIG.HS256.key().build();
        } else key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String issue(long userId, String type, String session, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer("leets-blog")
                .subject(Long.toString(userId))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("sid", session)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token, String type) {
        try {
            var c =
                    Jwts.parser()
                            .verifyWith(key)
                            .requireIssuer("leets-blog")
                            .require("type", type)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
            if (c.getExpiration() == null
                    || c.getSubject() == null
                    || c.get("sid", String.class) == null) throw new AuthFailure(UNAUTHORIZED);
            return c;
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthFailure(UNAUTHORIZED);
        }
    }

    public com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Claims verify(
            String raw, String type) {
        var claims = parse(raw, type);
        try {
            return new com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Claims(
                    Long.parseLong(claims.getSubject()), claims.get("sid", String.class));
        } catch (NumberFormatException e) {
            throw new AuthFailure(UNAUTHORIZED);
        }
    }
}
