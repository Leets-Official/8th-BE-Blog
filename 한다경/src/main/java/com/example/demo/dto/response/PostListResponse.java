package com.example.demo.dto.response;

import java.util.List;

public record PostListResponse(
        List<PostResponse> posts
) {
}
