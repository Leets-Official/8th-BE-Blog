package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.Report;
import com.leets_8th_be.kanghyeonwoo.entity.ReportStatus;
import com.leets_8th_be.kanghyeonwoo.entity.ReportResolutionAction;
import java.time.LocalDateTime;

public record ReportResolveResponse(Long reportId, ReportStatus status, ReportResolutionAction action,
                                    Long processorId, LocalDateTime resolvedAt,
                                    boolean targetAvailable, Boolean targetHidden) {
    public static ReportResolveResponse from(Report report, boolean available, Boolean hidden) {
        return new ReportResolveResponse(report.getId(), report.getStatus(), report.getAction(),
                report.getProcessor().getId(), report.getResolvedAt(), available, hidden);
    }
}
