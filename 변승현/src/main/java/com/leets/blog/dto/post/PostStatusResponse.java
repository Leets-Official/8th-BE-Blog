package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import com.leets.blog.entity.PostStatus;

public record PostStatusResponse(
        @Schema(description = "게시글 ID", example = "1") Long id,
        @Schema(description = "숨김 처리 후 게시글 상태", example = "HIDDEN") PostStatus status
) {}
