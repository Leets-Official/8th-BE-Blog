package com.leets_8th_be.kanghyeonwoo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "게시글 생성 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 제목", example = "Spring Boot와 Swagger 연동 가이드")
    private String title;

    @Schema(description = "게시글 본문", example = "Spring Boot 3.x에서 OpenAPI 3.0(Swagger)을 설정하는 방법입니다.")
    private String content;

    @Schema(description = "작성자 회원 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성 일시")
    private LocalDateTime createdAt;

}
