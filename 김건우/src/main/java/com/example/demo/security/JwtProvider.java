package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    private static final String ISSUER = "leets-auth";
    private static final String AUDIENCE = "leets-api";
    private static final String TYPE_CLAIM = "type";

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-ttl-seconds}") long accessTtlSeconds,
                       @Value("${jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        // 32바이트 미만이면 WeakKeyException 으로 서버 시작 단계에서 실패한다.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public String createAccessToken(Long userId) {
        return create(userId, ACCESS, accessTtlSeconds);
    }

    public String createRefreshToken(Long userId) {
        return create(userId, REFRESH, refreshTtlSeconds);
    }

    private String create(Long userId, String type, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // 같은 초에 발급해도 토큰 문자열이 겹치지 않게 함
                .subject(String.valueOf(userId))
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim(TYPE_CLAIM, type)          // access / refresh 구분 (Refresh 로 API 를 호출하는 것을 막음)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 서명, 만료, 발급자, 수신자, 토큰 종류를 모두 검증하고 회원 ID 를 돌려준다.
     * 하나라도 어긋나면 JwtException 이 발생한다. (빈 문자열은 호출하는 쪽에서 걸러야 한다)
     */
    public Long parse(String token, String expectedType) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .require(TYPE_CLAIM, expectedType)
                .build()
                .parseSignedClaims(token);

        // 허용 알고리즘을 HS256 으로 고정한다.
        // (주의: parser().sig().clear().add(..) 는 JJWT 0.12.6 에서 clear() 직후 빈 목록으로 레지스트리를 만들다가 항상 예외가 난다)
        if (!Jwts.SIG.HS256.getId().equals(jws.getHeader().getAlgorithm())) {
            throw new JwtException("허용하지 않는 서명 알고리즘입니다.");
        }

        Claims claims = jws.getPayload();

        if (claims.getSubject() == null || claims.getExpiration() == null) {
            throw new JwtException("필수 클레임(sub, exp)이 없습니다.");
        }
        return Long.valueOf(claims.getSubject());
    }
}
