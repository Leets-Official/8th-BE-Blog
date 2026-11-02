package com.leets.blog.dto.post;

import com.leets.blog.entity.Post;
import com.leets.blog.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 상세 정보")
public record PostDetailResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "4주차 과제 회고")
        String title,

        @Schema(description = "본문", example = "신고와 숨김 기능을 구현하면서 상태 변화를 고민했습니다.")
        String content,

        @Schema(description = "작성자 회원 ID", example = "101")
        Long authorId,

        @Schema(description = "게시글 상태 (ACTIVE: 공개, HIDDEN: 숨김)", example = "ACTIVE")
        PostStatus status,

        @Schema(description = "작성 시각", example = "2026-11-02T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "마지막 수정 시각", example = "2026-11-02T15:00:00")
        LocalDateTime updatedAt
) {

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getId(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
