package com.leets_8th_be.kanghyeonwoo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 150, message = "제목은 150자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "본문은 필수 입력 항목입니다.")
    private String content;

    @NotNull(message = "작성자 ID는 필수 입력 항목입니다.")
    private Long authorId;
}
