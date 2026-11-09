package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(
        @Schema(description = "로그인 또는 직전 재발급 응답에서 받은 서비스 Refresh Token",
                example = "받은_refreshToken_값")
        @NotBlank @Size(max = 128) String refreshToken
) {}
