package com.leets.mission.dto.comment;

import com.leets.mission.domain.comment.Comment;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 정보")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "2")
        Long id,

        @Schema(description = "댓글이 달린 게시글 ID", example = "1")
        Long postId,

        @Schema(description = "댓글 내용", example = "좋은 글 감사해요.")
        String content,

        @Schema(description = "작성자 닉네임", example = "홍길동")
        String authorNickname,

        @Schema(description = "작성 시각", example = "2026-11-02T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 시각", example = "2026-11-02T13:00:00")
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getMember().getNickname(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}