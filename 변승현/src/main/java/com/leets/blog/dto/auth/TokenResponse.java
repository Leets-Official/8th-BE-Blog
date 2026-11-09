package com.leets.blog.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "API 인증용 토큰. Authorization: Bearer {accessToken}") String accessToken,
        @Schema(description = "토큰 재발급용 토큰. 재발급 시 새 값으로 교체") String refreshToken,
        @Schema(description = "Access Token 유효 시간(초)", example = "900") long expiresIn
) {}
