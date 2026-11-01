package com.leets.blog.dto.report;

import com.leets.blog.entity.Report;
import com.leets.blog.entity.ReportStatus;

public record ReportResponse(Long id, Long userId, Long postId, Long commentId,
                             String reason, ReportStatus status) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getReporter().getId(),
                report.getPost() == null ? null : report.getPost().getId(),
                report.getComment() == null ? null : report.getComment().getId(),
                report.getReason(), report.getStatus());
    }
}
