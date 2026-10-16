package com.leets.choyeji.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 255, message = "제목은 255자 이하로 입력해주세요.") String title,
        @NotBlank(message = "내용을 입력해주세요.") String content
) {
}
