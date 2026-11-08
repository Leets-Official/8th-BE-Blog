package org.example._thbe.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.example._thbe.domain.member.enums.MemberRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private final SecretKey key;

    @Getter
    private final long accessExp;
    @Getter
    private final long refreshExp;
    public static final String JWT_ERROR="jwtError";

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExp,
            @Value("${jwt.refresh-expiration}") long refreshExp

        ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;
    }

    public String createAccessToken(String memberId, MemberRole role) {
        return createToken(memberId, role, accessExp, "access");
    }

    public String createRefreshToken(String memberId, MemberRole role) {
        return createToken(memberId, role, refreshExp, "refresh");
    }

    private String createToken(String memberId, MemberRole role, long expiredTime, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId)
                .id(UUID.randomUUID().toString())
                .claim("role", role.name())
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiredTime))
                .signWith(key)
                .compact();
    }


    public Long getExp(String accessToken) {
        Claims claims = getClaims(accessToken);
        if(claims == null) {
            return null;
        }

        return claims.getExpiration().getTime();
    }



    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        }

        catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
    public Claims getClaims(HttpServletRequest request,String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        }
        catch (ExpiredJwtException e) {
            request.setAttribute(JWT_ERROR,"TOKEN_EXPIRED");
            return null;
        }

        catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(JWT_ERROR,"TOKEN_INVALID");
            return null;
        }
    }






}
