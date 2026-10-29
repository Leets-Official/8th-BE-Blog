package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;

public record ReportSearchQuery(int page, int size, ReportState status,
                               ReportTarget target, Long targetId, String keyword) {
}
