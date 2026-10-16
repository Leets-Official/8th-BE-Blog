package com.example.demo.dto.post;

import com.example.demo.entity.Post;
import java.time.LocalDateTime;

public record PostCreateResponse(Long postId, LocalDateTime createdAt) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(post.getId(), post.getCreatedAt());
    }
}