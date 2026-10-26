package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.GetReportsUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportSearchQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;

public class GetReportsService implements GetReportsUseCase {
    private final ReportStore reports;

    public GetReportsService(ReportStore reports) {
        this.reports = reports;
    }

    @Override
    public ReportPage getReports(ReportSearchQuery query) {
        if (query.page() < 1 || query.size() < 1 || query.size() > 100
                || (query.targetId() != null && query.targetId() < 1)) {
            throw new InvalidReportQuery();
        }
        return reports.search(query);
    }
}
