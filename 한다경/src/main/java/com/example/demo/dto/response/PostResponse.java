package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.demo.domain.Post;

import java.time.LocalDateTime;

public record PostResponse(
        @Schema(description = "고유 ID", example = "1")
        Long id,
        @Schema(description = "게시글 제목 (필수, 공백 불가, 최대 50자)", example = "첫 게시글입니다")
        String title,
        @Schema(description = "게시글 본문 (필수, 공백 불가)", example = "안녕하세요")
        String content,
        @Schema(description = "작성자 회원 ID", example = "1")
        Long userId,
        @Schema(description = "생성 시각", example = "2026-10-30T12:00:00")
        LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-10-30T12:00:00")
        LocalDateTime updatedAt,
        @Schema(description = "비공개 여부. true이면 작성자만 조회할 수 있습니다.", example = "false")
        boolean isPrivate,
        @Schema(description = "삭제 시각. 삭제되지 않은 게시글은 null입니다.", nullable = true)
        LocalDateTime deletedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.isPrivate(),
                post.getDeletedAt()
        );
    }
}
