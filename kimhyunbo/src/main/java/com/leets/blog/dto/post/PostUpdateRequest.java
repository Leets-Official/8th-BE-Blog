package com.leets.blog.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotNull(message = "작성자 ID는 필수 입력 항목입니다.")
        Long authorId,

        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 255, message = "제목은 255자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "본문은 필수 입력 항목입니다.")
        String content
) {
}
