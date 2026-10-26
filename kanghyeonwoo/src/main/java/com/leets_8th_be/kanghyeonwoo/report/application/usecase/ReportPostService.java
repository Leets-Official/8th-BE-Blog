package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DuplicateReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportRequest;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;

public class ReportPostService implements ReportPostUseCase {
    private final UserProfileLookup users;
    private final GetPostUseCase posts;
    private final ReportStore reports;

    public ReportPostService(UserProfileLookup users, GetPostUseCase posts, ReportStore reports) {
        this.users = users;
        this.posts = posts;
        this.reports = reports;
    }

    @Override
    public ReportView report(ReportPostCommand command) {
        if (command.postId().value() < 1) throw new InvalidReportRequest();
        var reporter = users.findById(command.reporterId()).orElseThrow(ReporterNotFound::new);
        var post = posts.get(command.postId());
        if (reporter.id().equals(post.authorId())) {
            throw new com.leets_8th_be.kanghyeonwoo.report.application.exception.SelfReportForbidden();
        }
        if (reports.exists(reporter.id(), ReportTarget.POST, command.postId().value())) {
            throw new DuplicateReport();
        }
        return reports.save(Report.pendingPost(reporter.id(), command.postId().value(), command.reason(),
                post.title(), post.content()));
    }
}
