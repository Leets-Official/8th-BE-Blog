package com.example.demo.dto;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 숨김 응답")
public record PostHideResponse(
        @Schema(description = "게시글 ID", example = "1") Long postId,
        @Schema(description = "변경된 게시글 상태 (ACTIVE: 노출, HIDDEN: 숨김)", example = "HIDDEN") PostStatus status
) {
    public static PostHideResponse from(Post post) {
        return new PostHideResponse(post.getId(), post.getStatus());
    }
}
