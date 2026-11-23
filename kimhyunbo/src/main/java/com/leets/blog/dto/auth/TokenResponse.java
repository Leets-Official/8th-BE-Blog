package com.leets.blog.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발급된 토큰")
public record TokenResponse(
        @Schema(description = "토큰 타입 (요청 헤더에 'Bearer {accessToken}' 형식으로 사용)", example = "Bearer")
        String grantType,

        @Schema(description = "API 요청에 사용하는 Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Access Token 재발급에 사용하는 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "Access Token 유효 시간 (초)", example = "1800")
        long accessTokenExpiresIn
) {
}
