package com.example.demo.dto.post;

import com.example.demo.entity.Post;
import java.time.LocalDateTime;

public record PostSummaryResponse(Long postId, String title, Long authorId, LocalDateTime createdAt) {

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(), post.getTitle(), post.getMember().getId(), post.getCreatedAt());
    }
}