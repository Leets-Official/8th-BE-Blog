package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.Report;
import com.leets_8th_be.kanghyeonwoo.entity.ReportStatus;
import com.leets_8th_be.kanghyeonwoo.entity.ReportTargetType;

public record ReportCreateResponse(Long reportId, ReportTargetType targetType, Long targetId, ReportStatus status) {
    public static ReportCreateResponse from(Report report) {
        return new ReportCreateResponse(report.getId(), report.getTargetType(), report.getTargetId(), report.getStatus());
    }
}
