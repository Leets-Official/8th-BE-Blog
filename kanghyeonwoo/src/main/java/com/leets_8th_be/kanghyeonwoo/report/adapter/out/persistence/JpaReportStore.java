package com.leets_8th_be.kanghyeonwoo.report.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import org.springframework.stereotype.Repository;

@Repository
public class JpaReportStore implements ReportStore {
    private final ReportJpaRepository reports;

    public JpaReportStore(ReportJpaRepository reports) {
        this.reports = reports;
    }

    @Override
    public boolean exists(UserId reporterId, ReportTarget target, Long targetId) {
        return reports.existsByReporterIdAndTargetTypeAndTargetId(reporterId.value(), target, targetId);
    }

    @Override
    public ReportView save(Report report) {
        return toView(reports.saveAndFlush(ReportJpaEntity.from(report)));
    }

    private static ReportView toView(ReportJpaEntity entity) {
        return new ReportView(new ReportId(entity.getId()), entity.getTargetType(), entity.getTargetId(),
                entity.getStatus(), entity.getReason(), entity.getTargetTitleSnapshot(),
                entity.getTargetContentSnapshot());
    }
}
