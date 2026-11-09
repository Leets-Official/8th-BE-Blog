package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedCommentReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DuplicateReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportRequest;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.SelfReportForbidden;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;

public class ReportCommentService implements ReportCommentUseCase {
    private final UserProfileLookup users;
    private final GetCommentTargetUseCase comments;
    private final ReportStore reports;

    public ReportCommentService(UserProfileLookup users, GetCommentTargetUseCase comments, ReportStore reports) {
        this.users = users;
        this.comments = comments;
        this.reports = reports;
    }

    @Override
    public ReportView report(ReportCommentCommand command) {
        if (command.commentId().value() < 1) throw new InvalidReportRequest();
        var reporter = users.findById(command.reporterId()).orElseThrow(ReporterNotFound::new);
        var target = comments.getTarget(command.commentId());
        var comment = target.comment();
        if (target.postState() != PostState.ACTIVE) throw new PostNotFound();
        if (comment.deleted()) throw new DeletedCommentReport();
        if (comment.state() != CommentState.ACTIVE) throw new CommentNotFound();
        if (reporter.id().equals(comment.authorId())) throw new SelfReportForbidden();
        if (reports.exists(reporter.id(), ReportTarget.COMMENT, command.commentId().value())) {
            throw new DuplicateReport();
        }
        return reports.save(Report.pendingComment(reporter.id(), command.commentId().value(), command.reason(),
                comment.content()));
    }
}
