package com.example.demo.dto;

import com.example.demo.domain.post.PostVisibility;
import com.example.demo.domain.report.Report;
import com.example.demo.domain.report.ReportStatus;

import java.time.LocalDateTime;

public final class ModerationDto {
    private ModerationDto() {}

    public record ReportRequest(String reason) {}
    public record ResolveRequest(String result) {}
    public record VisibilityRequest(PostVisibility status) {}
    public record State(Long postId, PostVisibility status, Long acceptedCommentId) {}
    public record ReportView(Long reportId, String targetType, Long postId, Long commentId,
                             Long reporterId, String reason, ReportStatus status, String result,
                             Long resolvedById, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static ReportView from(Report report) {
            boolean postReport = report.getPost() != null;
            return new ReportView(report.getId(), postReport ? "POST" : "COMMENT",
                    postReport ? report.getPost().getId() : null,
                    report.getComment() == null ? null : report.getComment().getId(),
                    report.getReporter().getId(), report.getReason(), report.getStatus(), report.getResult(),
                    report.getResolvedBy() == null ? null : report.getResolvedBy().getId(),
                    report.getCreatedAt(), report.getUpdatedAt());
        }
    }
}
