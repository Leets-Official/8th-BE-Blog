package com.leets.mission.domain.post.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// 게시글 응답 DTO
public class PostResponseDTO {

    // 조회, 생성, 수정 응답 사용
    @Builder
    public record DetailDTO(
            Long postId,
            String title,
            String content,
            String writer,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    // 게시글 목록의 한 항목
    @Builder
    public record PreviewDTO(
            Long postId,
            String title,
            String writer,
            LocalDateTime createdAt
    ) {
    }

    // 게시글 목록 + 페이지 정보
    @Builder
    public record PreviewListDTO(
            List<PreviewDTO> posts,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
    }

    // 게시글 삭제 결과
    @Builder
    public record DeleteDTO(
            Long postId
    ) {
    }
}
