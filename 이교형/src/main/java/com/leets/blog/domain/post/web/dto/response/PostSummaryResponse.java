package com.leets.blog.domain.post.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 목록 조회의 게시글 한 줄 (Repository 쿼리의 constructor expression과 필드 순서·타입이 같아야 함)
@Schema(description = "게시글 목록의 게시글 한 줄")
public record PostSummaryResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "새 게시글")
        String title,

        @Schema(description = "작성자 닉네임", example = "길동이")
        String author,

        @Schema(description = "작성 시각", example = "2026-11-02T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "삭제되지 않은 댓글 수", example = "3")
        Long commentCount
) {
}
