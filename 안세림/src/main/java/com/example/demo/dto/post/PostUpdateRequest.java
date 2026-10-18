package com.example.demo.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotNull(message = "작성자 ID는 필수입니다.")
        Long authorId,

        @Size(max = 255, message = "제목은 255자 이하로 입력해 주세요.")
        String title,

        String content
) {
}