package com.leets.mission.dto.report;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        Long postId,
        Long commentId,

        @NotBlank(message = "신고 사유를 입력해 주세요.")
        @Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.")
        String reason
) {
    @AssertTrue(message = "신고 대상은 게시글 또는 댓글 중 하나만 지정해야 합니다.")
    public boolean isTargetValid() {
        return (postId == null) != (commentId == null);
    }
}