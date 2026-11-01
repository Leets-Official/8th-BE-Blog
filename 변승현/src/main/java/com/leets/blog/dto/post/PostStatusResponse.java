package com.leets.blog.dto.post;

import com.leets.blog.entity.PostStatus;

public record PostStatusResponse(Long id, PostStatus status) {}
