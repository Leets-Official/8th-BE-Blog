package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(description = "회원가입에 사용한 이메일", example = "leets@example.com")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "회원가입에 사용한 비밀번호",
                format = "password", example = "ExamplePassword123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(max = 128) String password
) {}
