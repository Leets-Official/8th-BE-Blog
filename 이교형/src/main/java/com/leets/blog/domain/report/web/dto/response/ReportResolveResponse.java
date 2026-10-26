package com.leets.blog.domain.report.web.dto.response;

import com.leets.blog.domain.report.entity.Report;
import com.leets.blog.domain.report.entity.ReportStatus;

import java.time.LocalDateTime;

// 신고 처리 응답
public record ReportResolveResponse(
        Long reportId,
        ReportStatus status,
        LocalDateTime processedAt
) {

    public static ReportResolveResponse from(Report report) {
        return new ReportResolveResponse(
                report.getId(),
                report.getStatus(),
                report.getProcessedAt()
        );
    }
}
