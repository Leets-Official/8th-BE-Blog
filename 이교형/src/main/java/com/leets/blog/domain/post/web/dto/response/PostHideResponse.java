package com.leets.blog.domain.post.web.dto.response;

import com.leets.blog.domain.post.entity.Post;
import com.leets.blog.domain.post.entity.PostStatus;

// 게시글 숨김 응답
public record PostHideResponse(
        Long postId,
        PostStatus status
) {

    public static PostHideResponse from(Post post) {
        return new PostHideResponse(post.getId(), post.getStatus());
    }
}
