package com.leets.mission.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 게시글 요청 DTO 모음
public class PostRequestDTO {

    // 게시글 생성 요청
    @Schema(name = "PostCreateRequest", description = "게시글 생성 요청")
    public record CreateDTO(
            @Schema(description = "작성자 ID", example = "1")
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @Schema(description = "제목 (100자 이하)", example = "첫 글")
            @NotBlank(message = "제목을 입력해주세요.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @Schema(description = "본문", example = "첫 글의 본문입니다.")
            @NotBlank(message = "본문을 입력해주세요.")
            String content
    ) {
    }

    // 게시글 전체 수정 요청 (PUT: 제목과 본문 모두 필수)
    @Schema(name = "PostUpdateRequest", description = "게시글 전체 수정 요청 (제목과 본문 모두 필수)")
    public record UpdateDTO(
            @Schema(description = "작성자 ID", example = "1")
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @Schema(description = "제목 (100자 이하)", example = "수정한 제목")
            @NotBlank(message = "제목을 입력해주세요.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @Schema(description = "본문", example = "수정한 본문입니다.")
            @NotBlank(message = "본문을 입력해주세요.")
            String content
    ) {
    }

    // 게시글 부분 수정 요청 (PATCH: null이면 기존 값 유지)
    @Schema(name = "PostPatchRequest", description = "게시글 부분 수정 요청 (보내지 않은 필드는 기존 값 유지, 제목과 본문 중 하나는 필수)")
    public record PatchDTO(
            @Schema(description = "작성자 ID", example = "1")
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @Schema(description = "제목 (100자 이하, 빈 값 불가)", example = "수정한 제목")
            @Pattern(regexp = "(?s).*\\S.*", message = "제목은 빈 값으로 수정할 수 없습니다.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @Schema(description = "본문 (빈 값 불가)", example = "수정한 본문입니다.")
            @Pattern(regexp = "(?s).*\\S.*", message = "본문은 빈 값으로 수정할 수 없습니다.")
            String content
    ) {
    }
}
