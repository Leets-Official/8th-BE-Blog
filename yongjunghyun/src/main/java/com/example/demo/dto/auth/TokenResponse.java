package com.example.demo.dto.auth;

// 로그인·재발급 응답. 클라이언트는 이후 요청 헤더에 "Authorization: Bearer {accessToken}"을 붙인다.
public record TokenResponse(
        String grantType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn     // 액세스 토큰 유효 시간(초)
) {
    public static TokenResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
        return new TokenResponse("Bearer", accessToken, refreshToken, accessTokenExpiresIn);
    }
}
