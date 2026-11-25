package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "MEMBER_EMAIL_EMPTY")
        String email,

        @NotBlank(message = "MEMBER_PASSWORD_EMPTY")
        String password
) {
}
