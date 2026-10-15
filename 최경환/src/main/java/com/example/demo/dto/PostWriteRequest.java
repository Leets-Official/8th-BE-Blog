package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostWriteRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 50, message = "제목은 50자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        String content,

        boolean isPrivate
) {
}
