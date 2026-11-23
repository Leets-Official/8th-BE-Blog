package com.example.demo.post.dto;

import com.example.demo.global.enums.IsPrivate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class PostResDTO {

    @Schema(description = "게시글 작성 응답")
    public record PostCreateResponse(
            @Schema(description = "생성된 게시글 ID", example = "1")
            Long postId
    ) {
    }

    @Schema(description = "게시글 조회 응답")
    public record PostGetResponse(
            @Schema(description = "게시글 ID", example = "1")
            Long id,
            @Schema(description = "게시글 제목", example = "첫 번째 게시글")
            String title,
            @Schema(description = "게시글 내용", example = "안녕하세요. 첫 글입니다.")
            String content,
            @Schema(description = "작성자 ID", example = "1")
            Long memberId,
            @Schema(description = "공개 여부", example = "PUBLIC")
            IsPrivate isPrivate,
            @Schema(description = "작성 시각", example = "2026-11-01T12:00:00")
            LocalDateTime createdAt,
            @Schema(description = "수정 시각", example = "2026-11-01T12:30:00")
            LocalDateTime updatedAt,
            @Schema(description = "삭제 시각 (삭제되지 않았으면 null)", example = "null", nullable = true)
            LocalDateTime deletedAt
    ) {
    }

    @Schema(description = "게시글 수정 응답")
    public record PostUpdateResponse(
            @Schema(description = "게시글 ID", example = "1")
            Long id,
            @Schema(description = "수정된 제목", example = "수정된 제목")
            String title,
            @Schema(description = "수정된 내용", example = "수정된 내용입니다.")
            String content
            ) {
    }

    @Schema(description = "게시글 삭제 응답")
    public record PostDeleteResponse(
            @Schema(description = "삭제된 게시글 ID", example = "1")
            Long id
    ){}

    @Schema(description = "게시글 목록 조회 응답")
    public record PostGetListResponse(
            @Schema(description = "게시글 목록")
            List<PostGetResponse> posts
    ) {}
}
