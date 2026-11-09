package com.leets.blog.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 작성자는 요청 본문이 아니라 로그인한 회원(Access Token)으로 정한다.
@Schema(description = "게시글 작성 요청")
public record PostCreateRequest(
        @Schema(description = "제목 (255자 이하)", example = "4주차 과제 회고")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 255, message = "제목은 255자 이하로 입력해주세요.")
        String title,

        @Schema(description = "본문", example = "신고와 숨김 기능을 구현하면서 상태 변화를 고민했습니다.")
        @NotBlank(message = "본문은 필수 입력 항목입니다.")
        String content
) {
}
