package com.leets.choyeji.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 10000, message = "댓글은 10000자 이하로 입력해주세요.") String content
) {
}
