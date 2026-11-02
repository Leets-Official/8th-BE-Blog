package com.example.demo.dto;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;

public record PostHideResponse(Long postId, PostStatus status) {
    public static PostHideResponse from(Post post) {
        return new PostHideResponse(post.getId(), post.getStatus());
    }
}
