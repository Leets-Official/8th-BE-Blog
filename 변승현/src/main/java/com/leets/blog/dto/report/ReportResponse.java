package com.leets.blog.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import com.leets.blog.entity.Report;
import com.leets.blog.entity.ReportStatus;

public record ReportResponse(
        @Schema(description = "신고 ID", example = "1") Long id,
        @Schema(description = "신고자 ID", example = "1") Long userId,
        @Schema(description = "신고 대상 게시글 ID. 댓글 신고이면 null", example = "1") Long postId,
        @Schema(description = "신고 대상 댓글 ID. 게시글 신고이면 null", nullable = true) Long commentId,
        @Schema(description = "신고 사유", example = "부적절한 내용입니다.") String reason,
        @Schema(description = "신고 상태: PENDING 또는 RESOLVED", example = "PENDING") ReportStatus status
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getReporter().getId(),
                report.getPost() == null ? null : report.getPost().getId(),
                report.getComment() == null ? null : report.getComment().getId(),
                report.getReason(), report.getStatus());
    }
}
