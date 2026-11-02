package com.leets.blog.domain.report.web.dto.response;

import com.leets.blog.domain.report.entity.Report;
import com.leets.blog.domain.report.entity.ReportReason;
import com.leets.blog.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 신고 생성 응답 (targetType: POST | COMMENT)
@Schema(description = "신고 생성 응답")
public record ReportResponse(

        @Schema(description = "신고 ID", example = "1")
        Long reportId,

        @Schema(description = "신고 대상 종류", allowableValues = {"POST", "COMMENT"}, example = "POST")
        String targetType,

        @Schema(description = "신고 대상 ID (게시글 또는 댓글 ID)", example = "1")
        Long targetId,

        @Schema(description = "신고 사유", example = "SPAM")
        ReportReason reason,

        @Schema(description = "처리 상태 (생성 시 항상 PENDING)", example = "PENDING")
        ReportStatus status,

        @Schema(description = "신고 시각", example = "2026-11-02T15:00:00")
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
