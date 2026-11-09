package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 작성(POST), 전체 수정(PUT) 공용
@Schema(description = "게시글 작성 / 전체 수정 요청")
public record PostRequest(
        @Schema(description = "요청자(작성자) ID. 수정 시에는 글 작성자와 같아야 함", example = "1")
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @Schema(description = "게시글 제목 (최대 255자)", example = "첫 번째 게시글")
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 255, message = "제목은 255자 이하여야 합니다.") String title,
        @Schema(description = "게시글 내용", example = "안녕하세요, 반갑습니다.")
        @NotBlank(message = "내용을 입력해주세요.") String content
) {}
