package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leets.blog.entity.Post;
import com.leets.blog.entity.PostStatus;
import java.time.LocalDateTime;

public record PostResponse(
        @Schema(description = "게시글 ID", example = "1") Long id,
        @Schema(description = "게시글 제목", example = "첫 번째 게시글") String title,
        @Schema(description = "게시글 내용", example = "게시글 내용입니다.") String content,
        @Schema(description = "작성자 ID", example = "1") Long userId,
        @JsonProperty("isPrivate") @Schema(description = "비공개 여부", example = "false") boolean isPrivate,
        @Schema(description = "게시글 상태: ACTIVE 또는 HIDDEN", example = "ACTIVE") PostStatus status,
        @Schema(description = "생성 시각", example = "2026-11-01T20:00:00") LocalDateTime createdAt,
        @Schema(description = "생성 또는 마지막 수정 시각", example = "2026-11-01T20:00:00") LocalDateTime updatedAt,
        @Schema(description = "삭제 시각. 삭제 전에는 null", nullable = true) LocalDateTime deletedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getUser().getId(), post.isPrivate(), post.getStatus(), post.getCreatedAt(),
                post.getUpdatedAt(), post.getDeletedAt());
    }
}
