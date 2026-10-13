package com.leets.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StringRepeatRequest(
        @Schema(description = "반환할 문자열", example = "hello") String value
) {
}
