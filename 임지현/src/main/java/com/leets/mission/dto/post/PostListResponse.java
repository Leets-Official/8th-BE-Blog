package com.leets.mission.dto.post;

import org.springframework.data.domain.Page;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record PostListResponse(
        @Schema(description = "게시글 목록")
        List<PostResponse> content,

        @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
        int pageNumber,

        @Schema(description = "페이지 크기", example = "10")
        int pageSize,

        @Schema(description = "전체 게시글 수", example = "40")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "마지막 페이지 여부", example = "True")
        boolean last
) {
    public static PostListResponse of(Page<PostResponse> page) {
        return new PostListResponse(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}