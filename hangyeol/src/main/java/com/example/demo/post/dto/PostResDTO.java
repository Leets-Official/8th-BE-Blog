package com.example.demo.post.dto;

import com.example.demo.global.enums.IsPrivate;

import java.time.LocalDateTime;
import java.util.List;

public class PostResDTO {

    public record PostCreateResponse(
            Long postId
    ) {
    }

    public record PostGetResponse(
            Long id,
            String title,
            String content,
            Long memberId,
            IsPrivate isPrivate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
    }

    public record PostUpdateResponse(
            Long id,
            String title,
            String content
            ) {
    }

    public record PostDeleteResponse(
            Long id
    ){}

    public record PostGetListResponse(
            List<PostGetResponse> posts
    ) {}
}
