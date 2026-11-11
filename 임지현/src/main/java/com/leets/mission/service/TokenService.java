package com.leets.mission.service;

import com.leets.mission.domain.auth.RefreshToken;
import com.leets.mission.domain.auth.RefreshTokenRepository;
import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.dto.auth.TokenResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse issue(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());

        saveOrRotateRefreshToken(member, refreshToken);

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProvider.getAccessExpirationMs() / 1000
        );
    }

    public TokenResponse refresh(String refreshToken) {
        Claims claims;

        try {
            claims = jwtProvider.parse(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        RefreshToken savedToken = refreshTokenRepository
                .findByMemberIdForUpdate(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (savedToken.getExpiresAt().isBefore(Instant.now())
                || !savedToken.getTokenHash().equals(hash(refreshToken))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 새 Refresh Token을 발급하고 저장된 해시도 새 값으로 교체합니다.
        return issue(member);
    }

    private void saveOrRotateRefreshToken(Member member, String refreshToken) {
        String tokenHash = hash(refreshToken);
        Instant expiresAt = Instant.now()
                .plusMillis(jwtProvider.getRefreshExpirationMs());

        refreshTokenRepository.findByMember_Id(member.getId())
                .ifPresentOrElse(
                        savedToken -> savedToken.rotate(tokenHash, expiresAt),
                        () -> refreshTokenRepository.save(
                                new RefreshToken(member, tokenHash, expiresAt)
                        )
                );
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
