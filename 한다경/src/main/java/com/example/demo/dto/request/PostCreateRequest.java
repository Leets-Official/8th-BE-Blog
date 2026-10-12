package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "INVALID_INPUT")
        @Size(max = 50, message = "TITLE_TOO_LONG")
        String title,

        @NotBlank(message = "INVALID_INPUT")
        String content,

        Boolean isPrivate
) {

    public boolean privateValue() {
        return Boolean.TRUE.equals(isPrivate);
    }
}
