package com.example.demo.dto.response;
import com.example.demo.domain.Report;
import com.example.demo.domain.ReportStatus;
public record ReportResponse(Long id, Long reporterId, Long postId, Long commentId,
                             String reason, ReportStatus status, boolean targetHidden) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getReporter().getId(),
                report.getPost() == null ? null : report.getPost().getId(),
                report.getComment() == null ? null : report.getComment().getId(),
                report.getReason(), report.getStatus(),
                report.getPost() != null ? report.getPost().isHidden() : report.getComment().isHidden());
    }
}
