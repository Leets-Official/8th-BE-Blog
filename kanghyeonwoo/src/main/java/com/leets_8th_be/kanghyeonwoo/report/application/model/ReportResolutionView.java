package com.leets_8th_be.kanghyeonwoo.report.application.model;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

import java.time.LocalDateTime;

public record ReportResolutionView(ReportId id, ReportState state, ReportAction action,
                                   UserId processorId, LocalDateTime resolvedAt,
                                   boolean targetAvailable, Boolean targetHidden) {
}
