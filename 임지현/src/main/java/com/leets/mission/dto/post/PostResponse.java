package com.leets.mission.dto.post;

import com.leets.mission.domain.post.Post;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 상세 정보")
public record PostResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "게시글 제목", example = "첫 게시글")
        String title,

        String content,

        @Schema(description = "작성자 닉네임", example = "홍길동")
        String authorNickname,

        @Schema(description = "조회수", example = "22")
        Integer viewCount,

        @Schema(description = "작성 시각", example = "2026-11-02T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 시각", example = "2026-11-02T13:00:00")
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember() != null ? post.getMember().getNickname() : "익명",
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}