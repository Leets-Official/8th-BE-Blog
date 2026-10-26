package com.leets.blog.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.leets.blog.entity.Post;
import com.leets.blog.entity.PostStatus;
import java.time.LocalDateTime;

public record PostResponse(Long id, String title, String content, Long userId,
                           @JsonProperty("isPrivate") boolean isPrivate,
                           PostStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt,
                           LocalDateTime deletedAt) {
    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getUser().getId(), post.isPrivate(), post.getStatus(), post.getCreatedAt(),
                post.getUpdatedAt(), post.getDeletedAt());
    }
}
