package com.leets_8th_be.kanghyeonwoo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "게시글 수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

    @Schema(description = "작성자 회원 ID (권한 검증용)", example = "1")
    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long authorId;

    @Schema(description = "수정할 게시글 제목", example = "수정된 게시글 제목입니다.", maxLength = 150)
    @NotBlank(message = "수정할 제목은 필수입니다.")
    @Size(max = 150, message = "제목은 150자 이내여야 합니다.")
    private String title;

    @Schema(description = "수정할 게시글 본문", example = "수정된 게시글 본문 내용입니다.")
    @NotBlank(message = "수정할 본문은 필수입니다.")
    private String content;
}
