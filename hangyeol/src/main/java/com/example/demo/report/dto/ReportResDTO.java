package com.example.demo.report.dto;

import com.example.demo.report.entity.ReportCommentId;
import com.example.demo.report.entity.ReportPostId;
import com.example.demo.report.enums.ReportStatus;

public class ReportResDTO {
    public record PostReportResponse (
            ReportPostId reportPostId
    ){}

    public record CommentReportResponse (
            ReportCommentId reportCommentId
    ){}

    public record PostReportStatusResponse(
            ReportPostId reportPostId,
            ReportStatus status
    ) {}
}
