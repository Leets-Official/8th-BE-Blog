package com.example.demo.dto;

import com.example.demo.entity.Report;
import com.example.demo.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 응답")
public record ReportResponse(
        @Schema(description = "신고 ID", example = "1") Long reportId,
        @Schema(description = "신고 대상 게시글 ID", example = "1") Long postId,
        @Schema(description = "신고 상태 (PENDING: 접수, RESOLVED: 처리 완료)", example = "PENDING") ReportStatus status,
        @Schema(description = "신고 사유", example = "광고성 게시글입니다.") String reason,
        @Schema(description = "접수 시각", example = "2026-11-02T10:30:00") LocalDateTime createdAt,
        @Schema(description = "처리 완료 시각 (처리 전에는 null)", example = "2026-11-02T11:00:00", nullable = true) LocalDateTime resolvedAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getId(), report.getPost().getId(), report.getStatus(),
                report.getReason(), report.getCreatedAt(), report.getResolvedAt());
    }
}
