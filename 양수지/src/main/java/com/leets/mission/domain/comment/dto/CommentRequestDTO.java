package com.leets.mission.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 댓글 요청 DTO 모음
public class CommentRequestDTO {

    // 댓글 작성 요청
    public record CreateDTO(
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @NotBlank(message = "댓글 내용을 입력해주세요.")
            @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
            String content
    ) {
    }
}
