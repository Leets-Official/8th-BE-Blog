package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 발급 응답")
public record TokenResponse(
        @Schema(description = "Access Token (짧은 수명). Authorization: Bearer {accessToken} 으로 전송") String accessToken,
        @Schema(description = "Refresh Token (긴 수명). 재발급 API 에서만 사용. 재발급할 때마다 새 값으로 교체됨") String refreshToken
) {}
