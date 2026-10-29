package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.example.demo.domain.Report;
import com.example.demo.domain.ReportStatus;
public record ReportResponse(
        @Schema(description = "고유 ID", example = "1")
        Long id,
        @Schema(description = "신고자 회원 ID", example = "2")
        Long reporterId,
        @Schema(description = "신고 대상 게시글 ID. 댓글 신고이면 null", example = "1")
        Long postId,
        @Schema(description = "신고 대상 댓글 ID. 게시글 신고이면 null", example = "1")
        Long commentId,
                             @Schema(description = "신고 사유 (필수, 공백 불가, 최대 255자)", example = "부적절한 내용입니다")
        String reason,
        @Schema(description = "신고 상태: PENDING(대기), APPROVED(승인), REJECTED(거절)", example = "PENDING")
        ReportStatus status,
        @Schema(description = "신고 대상의 숨김 여부", example = "false")
        boolean targetHidden
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getReporter().getId(),
                report.getPost() == null ? null : report.getPost().getId(),
                report.getComment() == null ? null : report.getComment().getId(),
                report.getReason(), report.getStatus(),
                report.getPost() != null ? report.getPost().isHidden() : report.getComment().isHidden());
    }
}
