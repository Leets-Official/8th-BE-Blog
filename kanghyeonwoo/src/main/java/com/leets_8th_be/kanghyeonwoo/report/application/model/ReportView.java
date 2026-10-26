package com.leets_8th_be.kanghyeonwoo.report.application.model;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;

public record ReportView(ReportId id, ReportTarget target, Long targetId, ReportState state,
                         String reason, String titleSnapshot, String contentSnapshot) {
}
