package org.example._thbe.domain.post.dto;


import org.example._thbe.domain.post.entity.Post;

import java.time.LocalDate;

public record PostUpdateResponse(Long postId, String title, String content, String member, LocalDate createdDate) {
    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getNickname(),
                post.getCreatedAt().toLocalDate()
        );
    }
}