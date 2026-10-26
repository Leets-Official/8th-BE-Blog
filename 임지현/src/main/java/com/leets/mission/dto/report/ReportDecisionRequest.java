package com.leets.mission.dto.report;

import com.leets.mission.domain.report.ReportStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ReportDecisionRequest(
        @NotNull(message = "신고 처리 결과는 필수입니다.")
        ReportStatus status
) {
    @AssertTrue(message = "처리 결과는 RESOLVED 또는 DISMISSED여야 합니다.")
    public boolean isDecisionValid() {
        return status == ReportStatus.RESOLVED
                || status == ReportStatus.DISMISSED;
    }
}