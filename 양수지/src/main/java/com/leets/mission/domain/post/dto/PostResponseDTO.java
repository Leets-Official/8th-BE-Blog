package com.leets.mission.domain.post.dto;

import lombok.Builder;

import java.time.LocalDateTime;

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
}
