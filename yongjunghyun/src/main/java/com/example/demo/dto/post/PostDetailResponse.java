package com.example.demo.dto.post;

import com.example.demo.entity.Post;
import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId, String title, String content, Long authorId,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(), post.getTitle(), post.getContent(), post.getMember().getId(),
                post.getCreatedAt(), post.getUpdatedAt());
    }
}