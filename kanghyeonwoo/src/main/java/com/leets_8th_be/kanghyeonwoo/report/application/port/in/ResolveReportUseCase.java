package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportResolutionView;

public interface ResolveReportUseCase {
    ReportResolutionView resolve(ResolveReportCommand command);
}
