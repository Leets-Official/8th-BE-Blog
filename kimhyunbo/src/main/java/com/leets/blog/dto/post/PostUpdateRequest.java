package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정 요청")
public record PostUpdateRequest(
        @Schema(description = "수정할 제목 (255자 이하)", example = "4주차 과제 회고 (수정)")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 255, message = "제목은 255자 이하로 입력해주세요.")
        String title,

        @Schema(description = "수정할 본문", example = "내용을 조금 더 보충했습니다.")
        @NotBlank(message = "본문은 필수 입력 항목입니다.")
        String content
) {
}
