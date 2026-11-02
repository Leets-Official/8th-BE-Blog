package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 삭제 결과")
public record PostDeleteResponse(
        @Schema(description = "삭제된 게시글 ID", example = "1")
        Long deletedPostId
) {
}
