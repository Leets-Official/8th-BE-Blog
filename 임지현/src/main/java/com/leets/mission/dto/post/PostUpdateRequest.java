package com.leets.mission.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record PostUpdateRequest(
        @Schema(description = "수정할 게시글 제목. 공백은 허용되지 않으며 최대 100자입니다.", example = "수정된 제목")
        @NotBlank(message = "제목은 필수 항목이며 공백일 수 없습니다.")
        @Size(max = 100, message = "제목은 공백 포함 100자 이하이어야 합니다.")
        String title,

        @Schema(description = "수정할 게시글 본문. 공백은 허용되지 않으며 최대 100,000자입니다.", example = "수정된 게시글 내용입니다.")
        @NotBlank(message = "본문은 필수 항목이며 공백일 수 없습니다.")
        @Size(max = 100000, message = "본문은 공백 포함 100,000자 이하이어야 합니다.")
        String content
) {}