package com.leets.blog.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @Schema(example = "user@example.com") @NotBlank @Email @Size(max = 254) String email,
        @Schema(example = "password123!") @NotBlank @Size(max = 20) String password
) {}
