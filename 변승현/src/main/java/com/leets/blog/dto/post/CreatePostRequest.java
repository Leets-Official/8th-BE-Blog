package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreatePostRequest(
        @Schema(example = "1", description = "과제용 작성자 ID") Long userId,
        @Schema(example = "첫 번째 게시글") String title,
        @Schema(example = "게시글 내용입니다.") String content,
        @Schema(example = "false") Boolean isPrivate
) {}
