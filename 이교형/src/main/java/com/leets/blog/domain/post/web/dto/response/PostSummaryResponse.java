package com.leets.blog.domain.post.web.dto.response;

import java.time.LocalDateTime;

// 목록 조회의 게시글 한 줄 (Repository 쿼리의 constructor expression과 필드 순서·타입이 같아야 함)
public record PostSummaryResponse(
        Long postId,
        String title,
        String author,
        LocalDateTime createdAt,
        Long commentCount
) {
}
