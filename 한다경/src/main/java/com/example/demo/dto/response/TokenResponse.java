package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "보호 API의 Authorization 헤더에 사용하는 서비스 JWT", example = "발급된_accessToken")
        String accessToken,
        @Schema(description = "재발급 요청 본문에 사용하는 일회용 서비스 토큰", example = "발급된_refreshToken")
        String refreshToken,
        @Schema(description = "Authorization 헤더의 인증 방식", example = "Bearer")
        String tokenType,
        @Schema(description = "발급 시점부터 Access Token이 유효한 시간(초)", example = "600")
        long expiresIn
) {}
