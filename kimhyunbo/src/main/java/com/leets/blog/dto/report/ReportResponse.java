package com.leets.blog.dto.report;

import com.leets.blog.entity.Report;
import com.leets.blog.entity.ReportStatus;
import com.leets.blog.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 정보")
public record ReportResponse(
        @Schema(description = "신고 ID", example = "1")
        Long id,

        @Schema(description = "신고 대상 종류 (POST: 게시글, COMMENT: 댓글)", example = "POST")
        ReportTargetType targetType,

        @Schema(description = "신고 대상 ID (게시글 ID 또는 댓글 ID)", example = "1")
        Long targetId,

        @Schema(description = "신고한 회원 ID", example = "102")
        Long reporterId,

        @Schema(description = "신고 사유", example = "부적절한 내용이 포함되어 있습니다.")
        String reason,

        @Schema(description = "신고 상태 (PENDING: 처리 대기, RESOLVED: 처리 완료)", example = "PENDING")
        ReportStatus status,

        @Schema(description = "신고 시각", example = "2026-11-02T14:30:00")
        LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReporter().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
