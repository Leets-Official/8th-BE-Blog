package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record ResolveReportCommand(ReportId reportId, UserId processorId,
                                  ReportAction action, String resolutionReason) {
}
