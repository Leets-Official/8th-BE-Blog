package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ReportResponse(Long reportId, Long reporterId, ReportTargetType targetType, Long targetId,
                             ReportStatus status, String reason, String targetTitleSnapshot,
                             String targetContentSnapshot, LocalDateTime createdAt, ReportResolutionAction action,
                             String resolutionReason, Long processorId, LocalDateTime resolvedAt) {
}
