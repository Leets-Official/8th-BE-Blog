package com.leets.mission.domain.post.dto;

import com.leets.mission.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// 게시글 응답 DTO
public class PostResponseDTO {

    // 조회, 생성, 수정 응답 사용
    @Schema(name = "PostDetailResponse", description = "게시글 상세")
    @Builder
    public record DetailDTO(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "제목", example = "첫 글")
            String title,

            @Schema(description = "본문", example = "첫 글의 본문입니다.")
            String content,

            @Schema(description = "작성자 이름", example = "양수지")
            String writer,

            @Schema(description = "작성 시각", example = "2026-11-02T17:00:00")
            LocalDateTime createdAt,

            @Schema(description = "수정 시각", example = "2026-11-02T17:30:00")
            LocalDateTime updatedAt
    ) {
    }

    // 게시글 목록의 한 항목
    @Schema(name = "PostPreviewResponse", description = "게시글 목록의 한 항목")
    @Builder
    public record PreviewDTO(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "제목", example = "첫 글")
            String title,

            @Schema(description = "작성자 이름", example = "양수지")
            String writer,

            @Schema(description = "작성 시각", example = "2026-11-02T17:00:00")
            LocalDateTime createdAt
    ) {
    }

    // 게시글 목록 + 페이지 정보
    @Schema(name = "PostPreviewListResponse", description = "게시글 목록과 페이지 정보")
    @Builder
    public record PreviewListDTO(
            @Schema(description = "게시글 목록")
            List<PreviewDTO> posts,

            @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
            int page,

            @Schema(description = "페이지 크기", example = "10")
            int size,

            @Schema(description = "전체 게시글 수", example = "25")
            long totalElements,

            @Schema(description = "전체 페이지 수", example = "3")
            int totalPages,

            @Schema(description = "다음 페이지 존재 여부", example = "true")
            boolean hasNext
    ) {
    }

    // 게시글 상태 변경 결과
    @Schema(name = "PostStatusResponse", description = "게시글 상태 변경 결과")
    @Builder
    public record StatusDTO(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "게시글 상태 (ACTIVE: 노출, HIDDEN: 숨김)", example = "HIDDEN")
            PostStatus status
    ) {
    }

    // 게시글 삭제 결과
    @Schema(name = "PostDeleteResponse", description = "게시글 삭제 결과")
    @Builder
    public record DeleteDTO(
            @Schema(description = "삭제된 게시글 ID", example = "1")
            Long postId
    ) {
    }
}
