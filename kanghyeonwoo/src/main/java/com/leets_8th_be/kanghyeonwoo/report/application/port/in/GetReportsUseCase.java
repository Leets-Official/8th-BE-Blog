package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;

public interface GetReportsUseCase {
    ReportPage getReports(ReportSearchQuery query);
}
