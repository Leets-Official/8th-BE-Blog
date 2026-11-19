package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
public record LikeResponse(
        @Schema(description = "대상의 전체 좋아요 수", example = "1")
        long likeCount,
        @Schema(description = "요청한 회원의 좋아요 여부", example = "true")
        boolean liked
) {
}
