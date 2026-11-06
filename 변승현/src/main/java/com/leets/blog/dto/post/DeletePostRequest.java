package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeletePostRequest(
        @Schema(description = "삭제할 게시글의 작성자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long userId
) {}
