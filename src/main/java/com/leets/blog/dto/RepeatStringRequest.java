package com.leets.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepeatStringRequest(
        @Schema(description = "두 응답 필드에 담을 문자열", example = "hello") String value
) {
}
