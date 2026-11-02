package com.leets.blog.domain.post.web.dto.response;

import com.leets.blog.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 생성·수정 응답
@Schema(description = "게시글 생성·수정 응답")
public record PostResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "새 게시글")
        String title,

        @Schema(description = "본문", example = "내용입니다")
        String content,

        @Schema(description = "작성 시각", example = "2026-11-02T15:00:00")
        LocalDateTime createdAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
