package com.leets.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문자열 반환 요청")
public record RepeatStringRequest(
        @Schema(description = "반환받을 문자열", example = "hello")
        String value
) {
}
