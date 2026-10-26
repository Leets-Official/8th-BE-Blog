package com.leets_8th_be.kanghyeonwoo.report.domain.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record Report(ReportId id, UserId reporterId, ReportTarget target, Long targetId,
                     String reason, String titleSnapshot, String contentSnapshot, ReportState state) {
    public Report {
        if (reporterId == null || target == null || targetId == null || targetId < 1
                || reason == null || reason.isBlank() || state == null) {
            throw new IllegalArgumentException("신고 정보가 올바르지 않습니다.");
        }
    }

    public static Report pendingPost(UserId reporterId, Long postId, String reason,
                                     String titleSnapshot, String contentSnapshot) {
        return new Report(null, reporterId, ReportTarget.POST, postId, reason,
                titleSnapshot, contentSnapshot, ReportState.PENDING);
    }

    public static Report pendingComment(UserId reporterId, Long commentId, String reason,
                                        String contentSnapshot) {
        return new Report(null, reporterId, ReportTarget.COMMENT, commentId, reason,
                null, contentSnapshot, ReportState.PENDING);
    }
}
