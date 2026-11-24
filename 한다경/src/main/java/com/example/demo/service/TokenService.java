package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.domain.RefreshToken;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import static com.example.demo.exception.ErrorCode.INVALID_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class TokenService {
    public static final String ISSUER = "leets-blog";
    public static final String AUDIENCE = "leets-blog-api";
    private static final Duration ACCESS_LIFETIME = Duration.ofMinutes(10);
    private static final Duration REFRESH_LIFETIME = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokens;
    private final MemberRepository members;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public TokenResponse issue(Member member) {
        Instant now = clock.instant();
        return issuePair(member, now, now.plus(REFRESH_LIFETIME));
    }

    @Transactional
    public TokenResponse refresh(String rawToken) {
        RefreshToken token = refreshTokens.findForUpdate(hash(rawToken))
                .orElseThrow(() -> new BusinessException(INVALID_REFRESH_TOKEN));
        Instant now = clock.instant();
        if (!token.isUsableAt(now)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }
        Member member = members.findByIdAndDeletedAtIsNull(token.getMember().getId())
                .orElseThrow(() -> new BusinessException(INVALID_REFRESH_TOKEN));
        token.markUsed(now);
        return issuePair(member, now, token.getExpiresAt());
    }

    private TokenResponse issuePair(Member member, Instant now, Instant refreshExpiry) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .subject(member.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ACCESS_LIFETIME))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(new RefreshToken(hash(refreshToken), member, refreshExpiry));
        return new TokenResponse(accessToken, refreshToken, "Bearer", ACCESS_LIFETIME.toSeconds());
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
