package com.leets.blog.domain.auth.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

// 로그인·재발급 공통 응답 (카카오 로그인도 같은 응답 사용 예정)
@Schema(description = "토큰 응답")
public record TokenResponse(

        @Schema(description = "토큰 타입 (Authorization 헤더에 'Bearer {accessToken}'으로 사용)", example = "Bearer")
        String grantType,

        @Schema(description = "Access Token (30분)")
        String accessToken,

        @Schema(description = "Refresh Token (14일, 재발급에 사용)")
        String refreshToken,

        @Schema(description = "Access Token 유효 시간(초)", example = "1800")
        long accessTokenExpiresIn
) {

    public static TokenResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
        return new TokenResponse("Bearer", accessToken, refreshToken, accessTokenExpiresIn);
    }
}
