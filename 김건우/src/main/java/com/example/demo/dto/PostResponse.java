package com.example.demo.dto;

import com.example.demo.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 상세 응답")
public record PostResponse(
        @Schema(description = "게시글 ID", example = "1") Long postId,
        @Schema(description = "제목", example = "첫 번째 게시글") String title,
        @Schema(description = "내용", example = "안녕하세요, 반갑습니다.") String content,
        @Schema(description = "작성자 닉네임", example = "tester1") String writer,
        @Schema(description = "작성 시각", example = "2026-11-02T10:30:00") LocalDateTime createdAt,
        @Schema(description = "마지막 수정 시각", example = "2026-11-02T10:30:00") LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getUser().getNickname(), post.getCreatedAt(), post.getUpdatedAt());
    }
}
