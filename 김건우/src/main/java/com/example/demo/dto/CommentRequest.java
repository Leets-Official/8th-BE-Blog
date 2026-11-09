package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 작성 요청")
public record CommentRequest(
        @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
        @NotBlank(message = "내용을 입력해주세요.") String content
) {}
