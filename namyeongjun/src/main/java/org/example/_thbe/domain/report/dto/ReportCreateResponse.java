package org.example._thbe.domain.report.dto;

import org.example._thbe.domain.report.entity.Report;
import org.example._thbe.domain.report.enums.ReportContentType;


public record ReportCreateResponse(String reason,
                              ReportContentType contentType,
                              Long contentId
) {
    public static ReportCreateResponse from(Report report) {
        return new ReportCreateResponse(
                report.getReason(),
                report.getContentType(),
                report.getContentId()
        );
    }
}
