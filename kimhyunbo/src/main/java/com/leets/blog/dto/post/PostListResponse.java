package com.leets.blog.dto.post;

import com.leets.blog.entity.Post;

import java.time.LocalDateTime;

public record PostListResponse(Long id, String title, Long authorId, LocalDateTime createdAt) {

    public static PostListResponse from(Post post) {
        return new PostListResponse(post.getId(), post.getTitle(), post.getMember().getId(), post.getCreatedAt());
    }
}
