package org.example._thbe.domain.report.dto;

import org.example._thbe.domain.report.entity.Report;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.domain.report.enums.ReportStatus;

import java.util.List;

public record ReportListResponse(List<ReportListDetail> reports ) {

    public static ReportListResponse from(List<Report> reports) {
        return new ReportListResponse(reports.stream()
                .map(ReportListDetail::from)
                .toList());
    }

    public record ReportListDetail(
            Long reportId,
            ReportContentType contentType,
            Long contentId,
            ReportStatus status,
            String reporterName
    ) {

        public static ReportListDetail from(Report report) {
            return new ReportListDetail(
                    report.getId(),
                    report.getContentType(),
                    report.getContentId(),
                    report.getStatus(),
                    report.getReporter().getNickname()
            );
        }
    }
}