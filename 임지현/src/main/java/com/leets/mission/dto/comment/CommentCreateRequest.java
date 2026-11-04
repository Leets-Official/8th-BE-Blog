package com.leets.mission.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record CommentCreateRequest(
        @Schema(description = "댓글 내용. 공백은 허용되지 않으며 최대 10,000자입니다.", example = "좋은 글 감사해요.")
        @NotBlank(message = "댓글 내용은 필수 항목이며 공백일 수 없습니다.")
        @Size(max = 10000, message = "댓글은 공백 포함 10,000자 이하여야 합니다.")
        String content
) {
}