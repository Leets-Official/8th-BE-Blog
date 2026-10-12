package com.example.demo.dto.response;

import com.example.demo.domain.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isPrivate,
        LocalDateTime deletedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.isPrivate(),
                post.getDeletedAt()
        );
    }
}
