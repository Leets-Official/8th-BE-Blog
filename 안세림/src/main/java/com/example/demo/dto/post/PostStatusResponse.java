package com.example.demo.dto.post;

import com.example.demo.entity.PostStatus;

public record PostStatusResponse(Long postId, PostStatus status) {
}