package com.leets.mission.dto.report;

import com.leets.mission.domain.report.Report;
import com.leets.mission.domain.report.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        String targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        if (report.getPost() != null) {
            return new ReportResponse(
                    report.getId(),
                    "POST",
                    report.getPost().getId(),
                    report.getReason(),
                    report.getStatus(),
                    report.getCreatedAt()
            );
        }

        return new ReportResponse(
                report.getId(),
                "COMMENT",
                report.getComment().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}