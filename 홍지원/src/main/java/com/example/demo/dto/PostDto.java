package com.example.demo.dto;

import com.example.demo.domain.post.Post;
import com.example.demo.controller.ApiExceptionHandler.ApiException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;

public final class PostDto {
    private PostDto() {}

    public record Request(String title, String content) {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Request from(JsonNode body) {
            if (!body.isObject() || !body.path("title").isTextual() || !body.path("content").isTextual()) {
                throw new ApiException(400, "REQUIRED_FIELD_MISSING", "제목과 내용을 올바른 문자열로 입력해 주세요.");
            }
            return new Request(body.get("title").textValue(), body.get("content").textValue());
        }
    }
    public record Author(Long memberId, String name) {}
    public record Summary(Long postId, String title, String author,
                          LocalDateTime createdAt, long commentCount) {}
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
