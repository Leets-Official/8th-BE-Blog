package com.leets.blog.domain.post.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 삭제 응답")
public record PostDeleteResponse(

        @Schema(description = "삭제한 게시글 ID", example = "1")
        Long postId
) {
}
