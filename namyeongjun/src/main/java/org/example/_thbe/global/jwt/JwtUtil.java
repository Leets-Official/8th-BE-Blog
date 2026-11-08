package org.example._thbe.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long accessExp;

    public JwtUtil(@Value("${jwt.secret}") String secret,@Value("${jwt.access-expiration}") long accessExp) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExp = accessExp;
    }

    public String createAccessToken() {
        Date now = new Date();
        Date expTime = new Date(now.getTime()+accessExp);

        return Jwts.builder()
                .subject()
                .claim()
                .issuedAt(now)
                .expiration(expTime)
                .signWith(key)
                .compact();
    }



}
