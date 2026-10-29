package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostListResponse(
        List<PostResponse> posts
) {
}
