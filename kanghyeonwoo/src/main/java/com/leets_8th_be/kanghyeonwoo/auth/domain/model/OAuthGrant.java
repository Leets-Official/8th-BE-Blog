package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

import java.time.Instant;

public record OAuthGrant(
        String hash,
        OAuthPurpose purpose,
        String browserHash,
        Instant expiresAt,
        String kakaoId,
        String email,
        Long userId) {}
