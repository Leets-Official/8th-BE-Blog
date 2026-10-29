package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.*;
import java.time.LocalDateTime;

public record ReportResponse(Long reportId, Long reporterId, ReportTargetType targetType, Long targetId,
                             ReportStatus status, String reason, String targetTitleSnapshot,
                             String targetContentSnapshot, LocalDateTime createdAt, ReportResolutionAction action,
                             String resolutionReason, Long processorId, LocalDateTime resolvedAt) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getReporter().getId(), report.getTargetType(),
                report.getTargetId(), report.getStatus(), report.getReason(), report.getTargetTitleSnapshot(),
                report.getTargetContentSnapshot(), report.getCreatedAt(), report.getAction(), report.getResolutionReason(),
                report.getProcessor() == null ? null : report.getProcessor().getId(), report.getResolvedAt());
    }
}
