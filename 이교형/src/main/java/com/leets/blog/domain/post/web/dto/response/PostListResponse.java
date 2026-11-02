package com.leets.blog.domain.post.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "게시글 목록 응답")
public record PostListResponse(

        @Schema(description = "게시글 목록")
        List<PostSummaryResponse> posts,

        @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 게시글 수 (삭제·숨김 제외)", example = "25")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

    public static PostListResponse from(Page<PostSummaryResponse> result) {
        return new PostListResponse(
                result.getContent(),
                result.getNumber() + 1,     // Spring은 0부터, 명세는 1부터
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }
}
