package com.leets.blog.dto.post;

import com.leets.blog.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 목록의 한 항목")
public record PostListResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "4주차 과제 회고")
        String title,

        @Schema(description = "작성자 회원 ID", example = "101")
        Long authorId,

        @Schema(description = "작성 시각", example = "2026-11-02T14:30:00")
        LocalDateTime createdAt
) {

    public static PostListResponse from(Post post) {
        return new PostListResponse(post.getId(), post.getTitle(), post.getMember().getId(), post.getCreatedAt());
    }
}
