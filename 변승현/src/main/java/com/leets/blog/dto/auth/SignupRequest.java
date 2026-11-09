package com.leets.blog.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record SignupRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "비밀번호. 8~20자", example = "password123!")
        @NotBlank @Size(min = 8, max = 20) String password,
        @Schema(description = "닉네임. 1~50자", example = "승현")
        @NotBlank @Size(max = 50) String nickname
) {}
