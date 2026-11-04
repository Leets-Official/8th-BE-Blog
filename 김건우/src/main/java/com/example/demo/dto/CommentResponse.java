package com.example.demo.dto;

import com.example.demo.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 응답")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "1") Long commentId,
        @Schema(description = "게시글 ID", example = "1") Long postId,
        @Schema(description = "작성자 닉네임", example = "tester2") String writer,
        @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.") String content,
        @Schema(description = "채택 여부 (게시글당 1개만 true 가능)", example = "false") boolean accepted,
        @Schema(description = "작성 시각", example = "2026-11-02T10:30:00") LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getPost().getId(),
                comment.getUser().getNickname(), comment.getContent(),
                comment.isAccepted(), comment.getCreatedAt());
    }
}
