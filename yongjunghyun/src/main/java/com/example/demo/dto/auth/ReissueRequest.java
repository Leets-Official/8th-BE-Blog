package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank(message = "REFRESH_TOKEN_EMPTY")
        String refreshToken
) {
}
