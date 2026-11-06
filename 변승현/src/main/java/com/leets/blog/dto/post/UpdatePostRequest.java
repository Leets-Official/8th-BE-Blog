package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePostRequest(
        @Schema(description = "게시글 작성자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long userId,
        @Schema(description = "수정할 제목. 공백 불가", example = "수정된 제목", requiredMode = Schema.RequiredMode.REQUIRED) String title,
        @Schema(description = "수정할 내용. 생략하거나 null이면 기존 내용 유지", example = "수정된 내용") String content
) {}
