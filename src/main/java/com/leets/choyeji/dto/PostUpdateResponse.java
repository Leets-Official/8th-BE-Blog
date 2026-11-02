package com.leets.choyeji.dto;

import com.leets.choyeji.entity.Post;
import com.leets.choyeji.entity.PostStatus;
import java.time.LocalDate;

public record PostUpdateResponse(Long postId, String title, String content, LocalDate createdDate, PostStatus status) {
    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getCreatedAt().toLocalDate(), post.getStatus());
    }
}
