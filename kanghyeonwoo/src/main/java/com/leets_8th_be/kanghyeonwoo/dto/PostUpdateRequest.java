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
public class PostUpdateRequest {

    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long authorId;

    @NotBlank(message = "수정할 제목은 필수입니다.")
    @Size(max = 150, message = "제목은 150자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "수정할 본문은 필수입니다.")
    private String content;
}
