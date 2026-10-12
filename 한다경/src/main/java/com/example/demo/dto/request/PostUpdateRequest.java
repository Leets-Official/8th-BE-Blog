package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank(message = "TITLE_REQUIRED")
        @Size(max = 50, message = "TITLE_TOO_LONG")
        String title,

        @NotBlank(message = "INVALID_INPUT")
        String content
) {
}
