package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "게시글 등록 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @Schema(description = "게시글 제목", example = "Spring Boot와 Swagger 연동 가이드", maxLength = 150)
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 150, message = "제목은 150자 이내여야 합니다.")
    private String title;

    @Schema(description = "게시글 본문", example = "Spring Boot 3.x에서 OpenAPI 3.0(Swagger)을 설정하는 방법입니다.")
    @NotBlank(message = "본문은 필수 입력 항목입니다.")
    private String content;

    @Schema(description = "작성자 회원 ID", example = "1")
    @NotNull(message = "작성자 ID는 필수 입력 항목입니다.")
    private Long authorId;
}
