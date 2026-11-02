package com.example.demo.converter;

import com.example.demo.dto.report.ReportResponse;
import com.example.demo.entity.Report;

public class ReportConverter {

    private ReportConverter() {
    }

    public static ReportResponse toResponse(Report report) {
        boolean isPostReport = report.getPost() != null;
        return new ReportResponse(
                report.getId(),
                isPostReport ? "POST" : "COMMENT",
                isPostReport ? report.getPost().getId() : report.getComment().getId(),
                report.getReporter().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt());
    }
}