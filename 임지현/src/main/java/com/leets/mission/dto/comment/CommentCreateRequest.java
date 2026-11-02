package com.leets.mission.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수 항목이며 공백일 수 없습니다.")
        @Size(max = 10000, message = "댓글은 공백 포함 10,000자 이하여야 합니다.")
        String content
) {
}