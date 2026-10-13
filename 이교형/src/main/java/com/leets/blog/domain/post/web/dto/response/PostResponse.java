package com.leets.blog.domain.post.web.dto.response;

import com.leets.blog.domain.post.entity.Post;

import java.time.LocalDateTime;

// 생성·수정 응답
public record PostResponse(
        Long postId,
        String title,
        String content,
        LocalDateTime createdAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
