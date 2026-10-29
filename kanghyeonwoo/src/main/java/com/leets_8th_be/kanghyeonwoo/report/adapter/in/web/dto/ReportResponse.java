package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "신고 목록 항목. 신고 접수 당시 대상 스냅샷을 포함합니다.")
public record ReportResponse(
        @Schema(description = "신고 ID", example = "100") Long reportId,
        @Schema(description = "신고자 사용자 ID", example = "2") Long reporterId,
        @Schema(description = "신고 대상 종류", example = "POST") ReportTargetType targetType,
        @Schema(description = "신고 대상 ID", example = "1") Long targetId,
        @Schema(description = "신고 상태", example = "PENDING") ReportStatus status,
        @Schema(description = "신고 사유", example = "욕설이 포함되어 있습니다.") String reason,
        @Schema(description = "신고 당시 대상 제목 스냅샷", example = "공지사항") String targetTitleSnapshot,
        @Schema(description = "신고 당시 대상 본문 스냅샷", example = "부적절한 표현이 포함된 본문") String targetContentSnapshot,
        @Schema(description = "신고 접수 일시", example = "2026-10-29T11:30:00") LocalDateTime createdAt,
        @Schema(description = "처리 행동. 미처리 신고면 null", example = "HIDE", nullable = true)
        ReportResolutionAction action,
        @Schema(description = "처리 사유. 미처리 신고면 null", example = "인신공격 확인", nullable = true)
        String resolutionReason,
        @Schema(description = "처리자 사용자 ID. 미처리 신고면 null", example = "1", nullable = true)
        Long processorId,
        @Schema(description = "처리 일시. 미처리 신고면 null", example = "2026-10-29T11:35:00", nullable = true)
        LocalDateTime resolvedAt) {
}
