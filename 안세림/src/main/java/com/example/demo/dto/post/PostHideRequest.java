package com.example.demo.dto.post;

import jakarta.validation.constraints.NotNull;

public record PostHideRequest(
        @NotNull(message = "작성자 ID는 필수입니다.")
        Long authorId
) {
}