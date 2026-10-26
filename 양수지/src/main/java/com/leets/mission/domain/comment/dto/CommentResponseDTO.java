package com.leets.mission.domain.comment.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// 댓글 응답 DTO
public class CommentResponseDTO {

    // 댓글 작성 응답, 목록의 한 항목
    @Builder
    public record DetailDTO(
            Long commentId,
            Long postId,
            String content,
            String writer,
            LocalDateTime createdAt
    ) {
    }

    // 게시글의 댓글 목록
    @Builder
    public record ListDTO(
            Long postId,
            List<DetailDTO> comments,
            int count
    ) {
    }
}
