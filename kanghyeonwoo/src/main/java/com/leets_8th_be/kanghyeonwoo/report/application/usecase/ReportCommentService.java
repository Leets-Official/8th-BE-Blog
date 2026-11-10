package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.report.application.exception.CommentTargetNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedCommentReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DuplicateReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportRequest;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ParentPostUnavailable;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.SelfReportForbidden;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetKind;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetPort;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReporterDirectory;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;

public class ReportCommentService implements ReportCommentUseCase {
    private final ReporterDirectory reporters;
    private final ReportTargetPort targets;
    private final ReportStore reports;

    public ReportCommentService(ReporterDirectory reporters, ReportTargetPort targets, ReportStore reports) {
        this.reporters = reporters;
        this.targets = targets;
        this.reports = reports;
    }

    @Override
    public ReportView report(ReportCommentCommand command) {
        if (command.commentId() < 1) {
            throw new InvalidReportRequest();
        }
        if (!reporters.exists(command.reporterId())) {
            throw new ReporterNotFound();
        }
        var comment =
                targets.find(ReportTargetKind.COMMENT, command.commentId()).orElseThrow(CommentTargetNotFound::new);
        if (!comment.parentPostActive()) {
            throw new ParentPostUnavailable();
        }
        if (comment.deleted()) {
            throw new DeletedCommentReport();
        }
        if (comment.hidden()) {
            throw new CommentTargetNotFound();
        }
        if (command.reporterId().equals(comment.authorId())) {
            throw new SelfReportForbidden();
        }
        if (reports.exists(command.reporterId(), ReportTarget.COMMENT, command.commentId())) {
            throw new DuplicateReport();
        }
        return reports.save(
                Report.pendingComment(command.reporterId(), command.commentId(), command.reason(), comment.content()));
    }
}
