package com.example.demo.dto.post;

import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @Size(max = 100) String title,
        String content
) {
}