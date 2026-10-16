package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "게시글 목록 요약 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 제목", example = "Spring Boot와 Swagger 연동 가이드")
    private String title;

    @Schema(description = "작성자 회원 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성 일시")
    private LocalDateTime createdAt;

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getId(),
                post.getCreatedAt()
        );
    }
}
