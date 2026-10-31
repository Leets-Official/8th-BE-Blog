package com.leets.choyeji.dto;

import com.leets.choyeji.entity.PostStatus;
import com.leets.choyeji.entity.Report;
import com.leets.choyeji.entity.ReportResult;
import com.leets.choyeji.entity.ReportStatus;
import java.time.LocalDateTime;

public record AdminReportResponse(Long id, Long postId, Long memberId, String reason,
                                  ReportStatus status, ReportResult result, PostStatus postStatus,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static AdminReportResponse from(Report report) {
        return new AdminReportResponse(report.getId(), report.getPost().getId(), report.getMember().getId(),
                report.getReason(), report.getStatus(), report.getResult(), report.getPost().getStatus(),
                report.getCreatedAt(), report.getUpdatedAt());
    }
}
