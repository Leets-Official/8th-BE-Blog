package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeletePostRequest(
        @Schema(example = "1", description = "과제용 요청자 ID") Long userId
) {}
