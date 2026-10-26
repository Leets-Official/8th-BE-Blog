package com.leets_8th_be.kanghyeonwoo.report.application.port.out;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportSearchQuery;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import java.util.Optional;

public interface ReportStore {
    boolean exists(UserId reporterId, ReportTarget target, Long targetId);

    ReportView save(Report report);

    Optional<Report> findForUpdate(ReportId id);

    ReportPage search(ReportSearchQuery query);
}
