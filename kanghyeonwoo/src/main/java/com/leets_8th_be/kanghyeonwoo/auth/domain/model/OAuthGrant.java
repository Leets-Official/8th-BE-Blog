package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import java.time.Instant;

public record OAuthGrant(
        String hash,
        OAuthPurpose purpose,
        String browserHash,
        Instant expiresAt,
        String kakaoId,
        String email,
        Long userId) {
    public void requireUsable(OAuthPurpose expected, String expectedBrowserHash, Instant now) {
        if (purpose != expected
                || !TokenFingerprint.matches(browserHash, expectedBrowserHash)
                || !expiresAt.isAfter(now)) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
    }

    public OAuthGrant withIdentity(String kakaoId, String email, Long userId) {
        return new OAuthGrant(hash, purpose, browserHash, expiresAt, kakaoId, email, userId);
    }
}
