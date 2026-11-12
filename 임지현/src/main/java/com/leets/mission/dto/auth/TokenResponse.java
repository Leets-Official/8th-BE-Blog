package com.leets.mission.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "서비스 토큰 응답")
public record TokenResponse(
        @Schema(description = "Access Token")
        String accessToken,

        @Schema(description = "Refresh Token")
        String refreshToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "Access Token 만료까지 남은 초", example = "900")
        long expiresIn
) {}