package com.example.demo.dto;

import com.example.demo.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 목록의 항목 (본문 제외)")
public record PostSummaryResponse(
        @Schema(description = "게시글 ID", example = "1") Long postId,
        @Schema(description = "제목", example = "첫 번째 게시글") String title,
        @Schema(description = "작성자 닉네임", example = "tester1") String writer
) {

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(post.getId(), post.getTitle(), post.getUser().getNickname());
    }
}
