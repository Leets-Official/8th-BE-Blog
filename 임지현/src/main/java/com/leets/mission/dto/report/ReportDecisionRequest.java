package com.leets.mission.dto.report;

import com.leets.mission.domain.report.ReportStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 신고 처리 요청")
public record ReportDecisionRequest(
        @Schema(description = "처리 결과. RESOLVED(승인) 또는 DISMISSED(기각)", example = "RESOLVED")
        @NotNull(message = "신고 처리 결과는 필수입니다.")
        ReportStatus status
) {
    @AssertTrue(message = "처리 결과는 RESOLVED 또는 DISMISSED여야 합니다.")
    public boolean isDecisionValid() {
        return status == ReportStatus.RESOLVED
                || status == ReportStatus.DISMISSED;
    }
}