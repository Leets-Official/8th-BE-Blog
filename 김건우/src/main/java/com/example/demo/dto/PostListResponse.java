package com.example.demo.dto;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public record PostListResponse(
        List<PostSummaryResponse> posts,
        int page,
        int size,
        int totalPages
) {
    public static PostListResponse from(Page<Post> result) {
        List<PostSummaryResponse> posts = result.getContent().stream()
                .map(PostSummaryResponse::from)
                .toList();
        return new PostListResponse(posts, result.getNumber(), result.getSize(), result.getTotalPages());
    }
}