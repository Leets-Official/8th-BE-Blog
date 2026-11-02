package com.leets.mission.dto.report;

import com.leets.mission.domain.report.Report;
import com.leets.mission.domain.report.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 정보")
public record ReportResponse(
        @Schema(description = "신고 ID", example = "3")
        Long id,

        @Schema(description = "신고 대상 유형 (POST 또는 COMMENT)", example = "POST")
        String targetType,

        @Schema(description = "신고 대상 ID", example = "1")
        Long targetId,

        @Schema(description = "신고 사유", example = "부적절한 표현이 포함되어 있습니다.")
        String reason,

        @Schema(description = "신고 상태 (PENDING, RESOLVED, DISMISSED)", example = "PENDING")
        ReportStatus status,

        @Schema(description = "신고 생성 시각", example = "2026-11-02T12:30:00")
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        if (report.getPost() != null) {
            return new ReportResponse(
                    report.getId(),
                    "POST",
                    report.getPost().getId(),
                    report.getReason(),
                    report.getStatus(),
                    report.getCreatedAt()
            );
        }

        return new ReportResponse(
                report.getId(),
                "COMMENT",
                report.getComment().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}