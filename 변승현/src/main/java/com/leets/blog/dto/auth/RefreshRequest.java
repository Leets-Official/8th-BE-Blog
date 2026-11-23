package com.leets.blog.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @Schema(description = "로그인 또는 재발급 시 받은 Refresh Token") @NotBlank String refreshToken
) {}
