package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 작성(POST), 전체 수정(PUT) 공용
public record PostRequest(
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 255, message = "제목은 255자 이하여야 합니다.") String title,
        @NotBlank(message = "내용을 입력해주세요.") String content
) {}