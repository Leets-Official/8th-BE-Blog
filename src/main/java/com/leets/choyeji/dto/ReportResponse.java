package com.leets.choyeji.dto;

import com.leets.choyeji.entity.Report;
import com.leets.choyeji.entity.ReportResult;
import com.leets.choyeji.entity.ReportStatus;
import java.time.LocalDateTime;

public record ReportResponse(Long id, Long postId, String reason, ReportStatus status,
                             ReportResult result, LocalDateTime createdAt) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getPost().getId(), report.getReason(),
                report.getStatus(), report.getResult(), report.getCreatedAt());
    }
}
