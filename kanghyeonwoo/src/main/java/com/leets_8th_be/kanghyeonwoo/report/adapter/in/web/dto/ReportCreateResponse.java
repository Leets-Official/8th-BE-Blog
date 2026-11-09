package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 접수 결과")
public record ReportCreateResponse(
        @Schema(description = "생성된 신고 ID", example = "100") Long reportId,
        @Schema(description = "신고 대상 종류", example = "POST") ReportTargetType targetType,
        @Schema(description = "신고 대상 ID", example = "1") Long targetId,
        @Schema(description = "접수 후 신고 상태", example = "PENDING") ReportStatus status) {
}
