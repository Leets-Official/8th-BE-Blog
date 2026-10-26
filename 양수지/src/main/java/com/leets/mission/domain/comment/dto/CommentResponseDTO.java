package com.leets.mission.domain.comment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

// 댓글 응답 DTO
public class CommentResponseDTO {

    // 댓글 작성 응답
    @Builder
    public record DetailDTO(
            Long commentId,
            Long postId,
            String content,
            String writer,
            LocalDateTime createdAt
    ) {
    }
}
