package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostListResponse(
        @Schema(description = "최신 생성 순으로 정렬한 공개 게시글 목록. 결과가 없으면 빈 배열을 반환합니다.")
        List<PostResponse> posts
) {
}
