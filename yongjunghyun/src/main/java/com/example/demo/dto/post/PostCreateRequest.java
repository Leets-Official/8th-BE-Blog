package com.example.demo.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// message에는 검증 실패 시 응답할 ErrorCode의 이름을 적는다 (GlobalExceptionHandler가 이 이름으로 찾음)
public record PostCreateRequest(
        @NotBlank(message = "POST_TITLE_EMPTY")
        @Size(max = 100, message = "POST_TITLE_TOO_LONG")
        String title,

        @NotBlank(message = "POST_CONTENT_EMPTY")
        String content
) {
}
