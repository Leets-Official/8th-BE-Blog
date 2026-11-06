package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PostListResponse(
        @Schema(description = "조회 가능한 게시글 목록. 최신 ID 순서") List<PostResponse> posts
) {}
