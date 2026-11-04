package com.example.demo.dto;

import com.example.demo.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "게시글 목록 응답 (페이징)")
public record PostListResponse(
        @Schema(description = "현재 페이지의 게시글 목록 (숨김 글 제외, 최신순)") List<PostSummaryResponse> posts,
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,
        @Schema(description = "페이지 크기", example = "10") int size,
        @Schema(description = "전체 페이지 수", example = "3") int totalPages
) {
    public static PostListResponse from(Page<Post> result) {
        List<PostSummaryResponse> posts = result.getContent().stream()
                .map(PostSummaryResponse::from)
                .toList();
        return new PostListResponse(posts, result.getNumber(), result.getSize(), result.getTotalPages());
    }
}
