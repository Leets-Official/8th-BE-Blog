package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePostRequest(
        @Schema(example = "1", description = "과제용 요청자 ID") Long userId,
        @Schema(example = "수정된 제목") String title,
        @Schema(example = "수정된 내용") String content
) {}
