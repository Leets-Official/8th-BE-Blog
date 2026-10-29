package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "게시글 댓글 목록 항목")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "10") Long id,
        @Schema(description = "작성자 사용자 ID", example = "2") Long authorId,
        @Schema(description = "부모 댓글 ID. 최상위 댓글이면 null", example = "3", nullable = true) Long parentId,
        @Schema(description = "댓글 내용 또는 숨김·삭제 안내 문구", example = "이 부분을 더 설명해 주세요.") String content,
        @Schema(description = "공개 상태", example = "ACTIVE") CommentStatus status,
        @Schema(description = "논리 삭제 여부", example = "false") boolean deleted,
        @Schema(description = "작성 일시", example = "2026-10-29T11:30:00") LocalDateTime createdAt) {
}
