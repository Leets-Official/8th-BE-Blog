package com.leets.mission.dto.report;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 생성 요청. postId와 commentId 중 하나만 지정해야 합니다.")
public record ReportCreateRequest(
        @Schema(description = "신고할 게시글 ID", example = "1")
        Long postId,

        @Schema(description = "신고할 댓글 ID", example = "2")
        Long commentId,

        @Schema(description = "신고 사유. 공백은 허용되지 않으며 최대 500자입니다.", example = "부적절한 표현이 포함되어 있습니다.")
        @NotBlank(message = "신고 사유를 입력해 주세요.")
        @Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.")
        String reason
) {
    @AssertTrue(message = "신고 대상은 게시글 또는 댓글 중 하나만 지정해야 합니다.")
    public boolean isTargetValid() {
        return (postId == null) != (commentId == null);
    }
}