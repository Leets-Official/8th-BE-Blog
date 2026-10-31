package com.leets.choyeji.dto;

import com.leets.choyeji.entity.ReportResult;
import jakarta.validation.constraints.NotNull;

public record ReportResolveRequest(
        @NotNull(message = "처리 결과를 입력해주세요. ACCEPTED 또는 REJECTED를 사용해주세요.") ReportResult result
) {
}
