package com.leets.blog.domain.report.web.dto.response;

import com.leets.blog.domain.report.entity.Report;
import com.leets.blog.domain.report.entity.ReportReason;
import com.leets.blog.domain.report.entity.ReportStatus;

import java.time.LocalDateTime;

// 신고 생성 응답 (targetType: POST | COMMENT)
public record ReportResponse(
        Long reportId,
        String targetType,
        Long targetId,
        ReportReason reason,
        ReportStatus status,
        LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        boolean isPost = report.getPost() != null;
        return new ReportResponse(
                report.getId(),
                isPost ? "POST" : "COMMENT",
                isPost ? report.getPost().getId() : report.getComment().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
