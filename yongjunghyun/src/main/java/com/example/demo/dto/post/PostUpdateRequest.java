package com.example.demo.dto.post;

import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @Size(max = 100, message = "POST_TITLE_TOO_LONG")
        String title,

        String content
) {
}
