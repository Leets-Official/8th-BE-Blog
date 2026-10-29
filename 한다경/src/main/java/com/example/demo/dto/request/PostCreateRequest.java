package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "INVALID_INPUT")
        @Size(max = 50, message = "TITLE_TOO_LONG")
        @Schema(description = "게시글 제목 (필수, 공백 불가, 최대 50자)", example = "첫 게시글입니다")
        String title,

        @NotBlank(message = "INVALID_INPUT")
        @Schema(description = "게시글 본문 (필수, 공백 불가)", example = "안녕하세요")
        String content,

        @Schema(description = "비공개 여부 (선택). 생략하거나 null이면 공개 게시글로 생성합니다.", example = "false")
        Boolean isPrivate
) {

    public boolean privateValue() {
        return Boolean.TRUE.equals(isPrivate);
    }
}
