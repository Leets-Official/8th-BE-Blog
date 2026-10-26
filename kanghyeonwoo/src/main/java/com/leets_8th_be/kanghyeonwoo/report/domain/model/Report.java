package com.leets_8th_be.kanghyeonwoo.report.domain.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import java.time.LocalDateTime;

public record Report(ReportId id, UserId reporterId, ReportTarget target, Long targetId,
                     String reason, String titleSnapshot, String contentSnapshot, ReportState state,
                     ReportAction action, String resolutionReason, UserId processorId, LocalDateTime resolvedAt) {
    public Report {
        if (reporterId == null || target == null || targetId == null || targetId < 1
                || reason == null || reason.isBlank() || state == null) {
            throw new IllegalArgumentException("신고 정보가 올바르지 않습니다.");
        }
    }

    public static Report pendingPost(UserId reporterId, Long postId, String reason,
                                     String titleSnapshot, String contentSnapshot) {
        return new Report(null, reporterId, ReportTarget.POST, postId, reason,
                titleSnapshot, contentSnapshot, ReportState.PENDING, null, null, null, null);
    }

    public static Report pendingComment(UserId reporterId, Long commentId, String reason,
                                        String contentSnapshot) {
        return new Report(null, reporterId, ReportTarget.COMMENT, commentId, reason,
                null, contentSnapshot, ReportState.PENDING, null, null, null, null);
    }

    public Report resolve(ReportAction action, String resolutionReason, UserId processorId, LocalDateTime resolvedAt) {
        if (state != ReportState.PENDING) throw new ReportAlreadyResolved();
        if (action == null || resolutionReason == null || resolutionReason.isBlank()
                || processorId == null || resolvedAt == null) {
            throw new IllegalArgumentException("신고 처리 정보가 올바르지 않습니다.");
        }
        return new Report(id, reporterId, target, targetId, reason, titleSnapshot, contentSnapshot,
                ReportState.RESOLVED, action, resolutionReason, processorId, resolvedAt);
    }
}
