package com.example.demo.dto;

import com.example.demo.domain.post.Post;
import com.example.demo.controller.ApiExceptionHandler.ApiException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public final class PostDto {
    private PostDto() {}

    @Schema(description = "게시글 생성·수정 요청")
    public record Request(
            @Schema(description = "게시글 제목", example = "스프링 부트 과제") String title,
            @Schema(description = "게시글 본문", example = "게시글 내용입니다.") String content) {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Request from(JsonNode body) {
            if (!body.isObject() || !body.path("title").isTextual() || !body.path("content").isTextual()) {
                throw new ApiException(400, "REQUIRED_FIELD_MISSING", "제목과 내용을 올바른 문자열로 입력해 주세요.");
            }
            return new Request(body.get("title").textValue(), body.get("content").textValue());
        }
    }
    public record Author(@Schema(description = "회원 ID", example = "1") Long memberId,
                         @Schema(description = "작성자 이름", example = "홍길동") String name) {}
    public record Summary(@Schema(description = "게시글 ID", example = "1") Long postId,
                          @Schema(description = "게시글 제목") String title,
                          @Schema(description = "작성자 이름") String author,
                          @Schema(description = "작성 시각", example = "2026-11-02T12:30:00") LocalDateTime createdAt,
                          @Schema(description = "댓글 수", example = "3") long commentCount) {}
    public record PostPage(List<Summary> posts, int page, int size,
                           long totalElements, int totalPages, boolean hasNext) {}
    public record Saved(Long postId, String title, String content, LocalDateTime createdAt) {
        public static Saved from(Post post) {
            return new Saved(post.getId(), post.getTitle(), post.getContent(), post.getCreatedAt());
        }
    }
    public record Detail(Long postId, String title, String content, LocalDateTime createdAt,
                         LocalDateTime updatedAt, LocalDateTime deletedAt, Author member) {
        public static Detail from(Post post) {
            return new Detail(post.getId(), post.getTitle(), post.getContent(), post.getCreatedAt(),
                    post.getUpdatedAt(), post.getDeletedAt(),
                    new Author(post.getMember().getId(), post.getMember().getName()));
        }
    }
    public record Deleted(Long postId) {}
}
