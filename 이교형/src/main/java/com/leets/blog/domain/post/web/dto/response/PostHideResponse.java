package com.leets.blog.domain.post.web.dto.response;

import com.leets.blog.domain.post.entity.Post;
import com.leets.blog.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 게시글 숨김 응답
@Schema(description = "게시글 숨김 응답")
public record PostHideResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        // example이 없으면 enum의 첫 값(ACTIVE)이 예시로 표시됨
        @Schema(description = "게시글 상태 (숨김 후 항상 HIDDEN)", example = "HIDDEN")
        PostStatus status
) {

    public static PostHideResponse from(Post post) {
        return new PostHideResponse(post.getId(), post.getStatus());
    }
}
