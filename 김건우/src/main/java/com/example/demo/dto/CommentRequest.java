package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "댓글 작성 요청")
public record CommentRequest(
        @Schema(description = "댓글 작성자 ID", example = "2")
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
        @NotBlank(message = "내용을 입력해주세요.") String content
) {}
