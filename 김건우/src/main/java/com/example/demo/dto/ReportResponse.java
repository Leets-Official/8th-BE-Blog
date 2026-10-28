package com.example.demo.dto;

import com.example.demo.entity.Report;
import com.example.demo.entity.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(
        Long reportId,
        Long postId,
        ReportStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getPost().getId(), report.getStatus(),
                report.getReason(), report.getCreatedAt(), report.getResolvedAt());
    }
}
