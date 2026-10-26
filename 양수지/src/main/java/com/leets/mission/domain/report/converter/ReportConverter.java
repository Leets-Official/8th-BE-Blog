package com.leets.mission.domain.report.converter;

import com.leets.mission.domain.report.dto.ReportRequestDTO;
import com.leets.mission.domain.report.dto.ReportResponseDTO;
import com.leets.mission.domain.report.entity.Report;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.domain.user.entity.User;

// 신고 Entity ↔ DTO 변환
public class ReportConverter {

    private ReportConverter() {
    }

    // 신고 요청 DTO → Report 엔티티
    public static Report toReport(ReportRequestDTO.CreateDTO request, User reporter,
                                  ReportTargetType targetType, Long targetId) {
        return Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason(request.reason())
                .build();
    }

    // Report 엔티티 → 상세 응답 DTO
    public static ReportResponseDTO.DetailDTO toDetailDTO(Report report) {
        return ReportResponseDTO.DetailDTO.builder()
                .reportId(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
