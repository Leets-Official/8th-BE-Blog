package org.example._thbe.domain.post.dto;

import org.example._thbe.domain.post.entity.Post;
import java.time.LocalDate;

public record PostCreateResponse(Long postId, String title, String content, String member, LocalDate createdDate) {
    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getNickname(),
                post.getCreatedAt().toLocalDate()
        );
    }
}
