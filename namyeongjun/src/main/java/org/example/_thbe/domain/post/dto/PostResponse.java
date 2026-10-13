package org.example._thbe.domain.post.dto;

import org.example._thbe.domain.post.entity.Post;

import java.time.LocalDate;

public record PostResponse(Long postId, String title, String content, String writer, LocalDate createdDate) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getNickname(),
                post.getCreatedAt().toLocalDate()
        );
    }
}
