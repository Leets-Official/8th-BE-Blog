package com.example.demo.report.dto;

import com.example.demo.report.enums.ReportStatus;

public class ReportReqDTO {
    public record PostReportRequest (
        Long reporterId,
        String reason
    ){}

    public record CommentReportRequest (
        Long reporterId,
        String reason

    ){}

    public record PostReportStatusRequest (
        ReportStatus status
    ) {}
}
