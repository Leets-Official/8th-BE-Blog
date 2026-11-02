package com.example.demo.dto;

import com.example.demo.domain.post.PostVisibility;
import com.example.demo.domain.report.Report;
import com.example.demo.domain.report.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public final class ModerationDto {
    private ModerationDto() {}

    @Schema(description = "신고 등록 요청")
    public record ReportRequest(@Schema(description = "신고 사유", example = "게시글 내용이 부적절합니다.") String reason) {}
    @Schema(description = "신고 처리 요청")
    public record ResolveRequest(@Schema(description = "처리 결과", example = "신고 내용을 확인했습니다.") String result) {}
    @Schema(description = "게시글 공개 상태 변경 요청")
    public record VisibilityRequest(@Schema(description = "변경할 공개 상태") PostVisibility status) {}
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
