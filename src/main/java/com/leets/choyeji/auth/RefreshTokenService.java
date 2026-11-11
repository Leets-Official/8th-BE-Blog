package com.leets.choyeji.auth;

import com.leets.choyeji.dto.LoginResponse;
import com.leets.choyeji.entity.RefreshToken;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    public static final long REFRESH_TOKEN_TTL_SECONDS = 14 * 24 * 60 * 60;
    private final SecureRandom random = new SecureRandom();
    private final RefreshTokenRepository tokens;
    private final MemberRepository members;
    private final JwtTokenService accessTokens;

    @Transactional
    public LoginResponse issue(Long memberId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new RefreshToken(hash(refreshToken), memberId,
                Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS)));
        return new LoginResponse(accessTokens.issue(memberId), refreshToken, "Bearer",
                JwtTokenService.ACCESS_TOKEN_TTL_SECONDS, REFRESH_TOKEN_TTL_SECONDS);
    }

    @Transactional
    public LoginResponse refresh(String rawToken) {
        RefreshToken token = tokens.findByHashForUpdate(hash(rawToken))
                .orElseThrow(ApiException::invalidRefreshToken);
        if (!token.getExpiresAt().isAfter(Instant.now()) || !members.existsById(token.getMemberId())) {
            throw ApiException.invalidRefreshToken();
        }
        // 이전 토큰은 한 번만 사용 가능하며, 잠금으로 동시 재발급을 차단합니다.
        tokens.delete(token);
        tokens.flush();
        return issue(token.getMemberId());
    }

    public static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
