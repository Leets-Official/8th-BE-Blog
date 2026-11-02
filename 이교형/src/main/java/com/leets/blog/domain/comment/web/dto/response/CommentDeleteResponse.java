package com.leets.blog.domain.comment.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 삭제 응답")
public record CommentDeleteResponse(

        @Schema(description = "삭제한 댓글 ID", example = "1")
        Long commentId
) {
}
