package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record ReissueRequest(
        @Schema(description = "로그인 또는 이전 재발급 때 받은 Refresh Token")
        @NotBlank(message = "refreshToken을 입력해주세요.") String refreshToken
) {}
