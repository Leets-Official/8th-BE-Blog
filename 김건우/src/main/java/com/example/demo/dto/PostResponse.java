package com.example.demo.dto;

import com.example.demo.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long postId,
        String title,
        String content,
        String writer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getUser().getNickname(), post.getCreatedAt(), post.getUpdatedAt());
    }
}