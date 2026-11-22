package com.leets.blog.common.security.jwt;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.member.entity.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

// JWT 발급·검증
@Component
public class JwtProvider {

    private static final String ROLE = "role";
    private static final String TYPE = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey key;

    // 키가 Base64가 아니거나 32바이트 미만이면 여기서 예외 발생
    public JwtProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    // Access Token: 회원 ID(sub) + 권한(role)
    public String createAccessToken(Long memberId, MemberRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(ROLE, role.name())
                .claim(TYPE, ACCESS)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.accessTokenValidity().toMillis()))
                .signWith(key)
                .compact();
    }

    // Refresh Token: 회원 ID(sub)만 담음
    // jti(UUID): JWT 시각은 초 단위라, 같은 초에 재발급하면 이전과 똑같은 문자열이 나와 재사용을 구분할 수 없음
    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(memberId))
                .claim(TYPE, REFRESH)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.refreshTokenValidity().toMillis()))
                .signWith(key)
                .compact();
    }

    // Access Token 검증: 만료면 EXPIRED_TOKEN(재발급하면 됨), 위조·형식 오류·Refresh Token이면 INVALID_TOKEN
    public AccessTokenClaims parseAccessToken(String token) {
        try {
            Claims claims = parse(token);
            if (!ACCESS.equals(claims.get(TYPE, String.class))) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
            return new AccessTokenClaims(
                    Long.valueOf(claims.getSubject()),
                    MemberRole.valueOf(claims.get(ROLE, String.class)));
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    // Refresh Token 검증: 만료·위조·Access Token이면 모두 INVALID_REFRESH_TOKEN (다시 로그인)
    public Long parseRefreshToken(String token) {
        try {
            Claims claims = parse(token);
            if (!REFRESH.equals(claims.get(TYPE, String.class))) {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record AccessTokenClaims(Long memberId, MemberRole role) {
    }
}
