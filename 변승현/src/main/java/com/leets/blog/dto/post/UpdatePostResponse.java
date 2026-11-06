package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
public record UpdatePostResponse(
        @Schema(description = "게시글 ID", example = "1") Long id,
        @Schema(description = "수정된 제목", example = "수정된 제목") String title,
        @Schema(description = "수정 후 게시글 내용", example = "수정된 내용") String content
) {}
