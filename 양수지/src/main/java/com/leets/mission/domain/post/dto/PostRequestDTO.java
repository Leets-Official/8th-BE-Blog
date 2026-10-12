package com.leets.mission.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 게시글 요청 DTO 모음
public class PostRequestDTO {

    // 게시글 생성 요청
    public record CreateDTO(
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @NotBlank(message = "제목을 입력해주세요.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @NotBlank(message = "본문을 입력해주세요.")
            String content
    ) {
    }
}
