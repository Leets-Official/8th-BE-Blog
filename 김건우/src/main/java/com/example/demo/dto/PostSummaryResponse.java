package com.example.demo.dto;

import com.example.demo.entity.Post;

public record PostSummaryResponse(Long postId, String title, String writer) {

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(post.getId(), post.getTitle(), post.getUser().getNickname());
    }
}