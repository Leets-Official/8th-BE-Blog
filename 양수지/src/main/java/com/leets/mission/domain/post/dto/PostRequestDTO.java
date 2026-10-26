package com.leets.mission.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    // 게시글 전체 수정 요청 (PUT: 제목과 본문 모두 필수)
    public record UpdateDTO(
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @NotBlank(message = "제목을 입력해주세요.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @NotBlank(message = "본문을 입력해주세요.")
            String content
    ) {
    }

    // 게시글 부분 수정 요청 (PATCH: null이면 기존 값 유지)
    public record PatchDTO(
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @Pattern(regexp = "(?s).*\\S.*", message = "제목은 빈 값으로 수정할 수 없습니다.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @Pattern(regexp = "(?s).*\\S.*", message = "본문은 빈 값으로 수정할 수 없습니다.")
            String content
    ) {
    }
}
