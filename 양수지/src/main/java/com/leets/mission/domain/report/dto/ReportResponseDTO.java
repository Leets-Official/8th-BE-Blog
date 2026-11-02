package com.leets.mission.domain.report.dto;

import com.leets.mission.domain.report.entity.ReportStatus;
import com.leets.mission.domain.report.entity.ReportTargetType;
import lombok.Builder;

import java.time.LocalDateTime;

// 신고 응답 DTO
public class ReportResponseDTO {

    // 신고 생성, 처리 응답
    @Builder
    public record DetailDTO(
            Long reportId,
            ReportTargetType targetType,
            Long targetId,
            String reason,
            ReportStatus status,
            LocalDateTime createdAt
    ) {
    }
}
