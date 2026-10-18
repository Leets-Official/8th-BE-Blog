package com.example.demo.converter;

import com.example.demo.dto.post.PostDetailResponse;
import com.example.demo.dto.post.PostListResponse;
import com.example.demo.entity.Post;

public class PostConverter {

    private PostConverter() {
    }

    public static PostListResponse toListResponse(Post post) {
        return new PostListResponse(post.getId(), post.getTitle(), post.getMember().getId(), post.getCreatedAt());
    }

    public static PostDetailResponse toDetailResponse(Post post) {
        return new PostDetailResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getMember().getId(), post.getCreatedAt(), post.getUpdatedAt());
    }
}