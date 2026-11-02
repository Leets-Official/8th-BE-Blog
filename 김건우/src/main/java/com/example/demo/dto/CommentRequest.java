package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @NotBlank(message = "내용을 입력해주세요.") String content
) {}
