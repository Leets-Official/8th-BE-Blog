package com.leets.mission.domain.report.dto;

import com.leets.mission.domain.report.entity.ReportStatus;
import com.leets.mission.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

// 신고 응답 DTO
public class ReportResponseDTO {

    // 신고 생성, 처리 응답
    @Schema(name = "ReportDetailResponse", description = "신고 상세")
    @Builder
    public record DetailDTO(
            @Schema(description = "신고 ID", example = "1")
            Long reportId,

            @Schema(description = "신고 대상 종류 (POST: 게시글, COMMENT: 댓글)", example = "POST")
            ReportTargetType targetType,

            @Schema(description = "신고 대상 ID", example = "1")
            Long targetId,

            @Schema(description = "신고 사유", example = "부적절한 내용입니다.")
            String reason,

            @Schema(description = "신고 상태 (PENDING: 처리 대기, RESOLVED: 승인, REJECTED: 반려)", example = "PENDING")
            ReportStatus status,

            @Schema(description = "신고 시각", example = "2026-11-02T17:00:00")
            LocalDateTime createdAt
    ) {
    }
}
