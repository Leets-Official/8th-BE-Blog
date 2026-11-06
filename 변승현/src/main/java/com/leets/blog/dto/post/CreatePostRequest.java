package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreatePostRequest(
        @Schema(description = "DB에 존재하는 작성자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long userId,
        @Schema(description = "게시글 제목. 공백 불가, 최대 50자", example = "첫 번째 게시글", requiredMode = Schema.RequiredMode.REQUIRED) String title,
        @Schema(description = "게시글 내용. 공백 불가", example = "게시글 내용입니다.", requiredMode = Schema.RequiredMode.REQUIRED) String content,
        @Schema(description = "비공개 여부. 생략하거나 null이면 공개", example = "false") Boolean isPrivate
) {}
