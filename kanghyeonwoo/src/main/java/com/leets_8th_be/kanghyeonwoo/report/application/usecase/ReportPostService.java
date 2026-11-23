package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.report.application.exception.DuplicateReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportRequest;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.PostTargetNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.SelfReportForbidden;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetKind;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetPort;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReporterDirectory;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;

public class ReportPostService implements ReportPostUseCase {
    private final ReporterDirectory reporters;
    private final ReportTargetPort targets;
    private final ReportStore reports;

    public ReportPostService(ReporterDirectory reporters, ReportTargetPort targets, ReportStore reports) {
        this.reporters = reporters;
        this.targets = targets;
        this.reports = reports;
    }

    @Override
    public ReportView report(ReportPostCommand command) {
        if (command.postId() < 1) {
            throw new InvalidReportRequest();
        }
        if (!reporters.exists(command.reporterId())) {
            throw new ReporterNotFound();
        }
        var post = targets.find(ReportTargetKind.POST, command.postId()).orElseThrow(PostTargetNotFound::new);
        if (post.deleted() || post.hidden()) {
            throw new PostTargetNotFound();
        }
        if (command.reporterId().equals(post.authorId())) {
            throw new SelfReportForbidden();
        }
        if (reports.exists(command.reporterId(), ReportTarget.POST, command.postId())) {
            throw new DuplicateReport();
        }
        return reports.save(Report.pendingPost(
                command.reporterId(), command.postId(), command.reason(), post.title(), post.content()));
    }
}
