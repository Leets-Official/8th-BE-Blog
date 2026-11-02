package com.leets.blog.domain.report.web.dto.response;

import com.leets.blog.domain.report.entity.Report;
import com.leets.blog.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 신고 처리 응답
@Schema(description = "신고 처리 응답")
public record ReportResolveResponse(

        @Schema(description = "신고 ID", example = "1")
        Long reportId,

        // example이 없으면 enum의 첫 값(PENDING)이 예시로 표시됨
        @Schema(description = "처리 상태 (처리 후 항상 RESOLVED)", example = "RESOLVED")
        ReportStatus status,

        @Schema(description = "처리 시각", example = "2026-11-02T16:00:00")
        LocalDateTime processedAt
) {

    public static ReportResolveResponse from(Report report) {
        return new ReportResolveResponse(
                report.getId(),
                report.getStatus(),
                report.getProcessedAt()
        );
    }
}
