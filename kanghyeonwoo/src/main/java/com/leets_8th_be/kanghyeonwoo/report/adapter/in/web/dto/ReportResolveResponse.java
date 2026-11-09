package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "신고 처리 결과")
public record ReportResolveResponse(
        @Schema(description = "처리한 신고 ID", example = "100") Long reportId,
        @Schema(description = "처리 후 신고 상태", example = "RESOLVED") ReportStatus status,
        @Schema(description = "처리 행동", example = "HIDE") ReportResolutionAction action,
        @Schema(description = "처리자 사용자 ID", example = "1") Long processorId,
        @Schema(description = "처리 일시", example = "2026-10-29T11:35:00") LocalDateTime resolvedAt,
        @Schema(description = "신고 대상이 현재 존재하는지 여부", example = "true") boolean targetAvailable,
        @Schema(description = "신고 대상이 숨김 상태인지 여부. 대상이 삭제된 경우 null", example = "true", nullable = true)
        Boolean targetHidden) {
}
