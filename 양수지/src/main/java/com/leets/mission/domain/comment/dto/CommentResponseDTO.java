package com.leets.mission.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// 댓글 응답 DTO
public class CommentResponseDTO {

    // 댓글 작성 응답, 목록의 한 항목
    @Schema(name = "CommentDetailResponse", description = "댓글 상세")
    @Builder
    public record DetailDTO(
            @Schema(description = "댓글 ID", example = "1")
            Long commentId,

            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "댓글 내용", example = "첫 댓글")
            String content,

            @Schema(description = "작성자 이름", example = "양수지")
            String writer,

            @Schema(description = "작성 시각", example = "2026-11-02T17:00:00")
            LocalDateTime createdAt
    ) {
    }

    // 게시글의 댓글 목록
    @Schema(name = "CommentListResponse", description = "게시글의 댓글 목록")
    @Builder
    public record ListDTO(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "댓글 목록")
            List<DetailDTO> comments,

            @Schema(description = "댓글 수", example = "3")
            int count
    ) {
    }
}
