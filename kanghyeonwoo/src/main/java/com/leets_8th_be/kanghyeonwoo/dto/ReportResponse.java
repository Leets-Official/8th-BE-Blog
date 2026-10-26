package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.ReportResolutionAction;
import com.leets_8th_be.kanghyeonwoo.entity.ReportStatus;
import com.leets_8th_be.kanghyeonwoo.entity.ReportTargetType;
import java.time.LocalDateTime;

public record ReportResponse(Long reportId, Long reporterId, ReportTargetType targetType, Long targetId,
                             ReportStatus status, String reason, String targetTitleSnapshot,
                             String targetContentSnapshot, LocalDateTime createdAt, ReportResolutionAction action,
                             String resolutionReason, Long processorId, LocalDateTime resolvedAt) {
}
