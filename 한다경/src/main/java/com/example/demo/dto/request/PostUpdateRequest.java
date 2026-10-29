package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank(message = "TITLE_REQUIRED")
        @Size(max = 50, message = "TITLE_TOO_LONG")
        @Schema(description = "게시글 제목 (필수, 공백 불가, 최대 50자)", example = "첫 게시글입니다")
        String title,

        @NotBlank(message = "INVALID_INPUT")
        @Schema(description = "게시글 본문 (필수, 공백 불가)", example = "수정한 본문입니다")
        String content
) {
}
