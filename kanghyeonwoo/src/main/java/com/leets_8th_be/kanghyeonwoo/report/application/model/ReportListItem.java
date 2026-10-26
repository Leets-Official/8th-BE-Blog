package com.leets_8th_be.kanghyeonwoo.report.application.model;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

import java.time.LocalDateTime;

public record ReportListItem(ReportId id, UserId reporterId, ReportTarget target, Long targetId,
                             ReportState state, String reason, String titleSnapshot, String contentSnapshot,
                             LocalDateTime createdAt, ReportAction action, String resolutionReason,
                             UserId processorId, LocalDateTime resolvedAt) {
}
