package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReportNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportResolutionView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import java.time.Clock;
import java.time.LocalDateTime;

public class ResolveReportService implements ResolveReportUseCase {
    private final UserProfileLookup users;
    private final ReportStore reports;
    private final InspectPostUseCase posts;
    private final HidePostUseCase hidePost;
    private final InspectCommentTargetUseCase comments;
    private final HideCommentUseCase hideComment;
    private final Clock clock;

    public ResolveReportService(UserProfileLookup users, ReportStore reports, InspectPostUseCase posts,
                                HidePostUseCase hidePost, InspectCommentTargetUseCase comments,
                                HideCommentUseCase hideComment, Clock clock) {
        this.users = users;
        this.reports = reports;
        this.posts = posts;
        this.hidePost = hidePost;
        this.comments = comments;
        this.hideComment = hideComment;
        this.clock = clock;
    }

    @Override
    public ReportResolutionView resolve(ResolveReportCommand command) {
        var processor = users.findById(command.processorId()).orElseThrow(ReporterNotFound::new);
        Report pending = reports.findForUpdate(command.reportId()).orElseThrow(ReportNotFound::new);
        Report resolved = pending.resolve(command.action(), command.resolutionReason(), processor.id(),
                LocalDateTime.now(clock));
        TargetVisibility visibility = pending.target() == ReportTarget.POST
                ? resolvePost(pending.targetId(), command.action())
                : resolveComment(pending.targetId(), command.action());
        reports.save(resolved);
        return new ReportResolutionView(resolved.id(), resolved.state(), resolved.action(), resolved.processorId(),
                resolved.resolvedAt(), visibility.available(), visibility.hidden());
    }

    private TargetVisibility resolvePost(Long postId, ReportAction action) {
        var post = posts.inspect(new PostId(postId)).orElse(null);
        if (post == null || post.state() == PostState.DELETED) return deletedTarget(action);
        if (action == ReportAction.HIDE) {
            var hidden = hidePost.hide(new PostId(postId));
            if (hidden.state() == PostState.DELETED) return deletedTarget(action);
            return new TargetVisibility(true, true);
        }
        return new TargetVisibility(true, post.state() == PostState.HIDDEN);
    }

    private TargetVisibility resolveComment(Long commentId, ReportAction action) {
        var target = comments.inspect(new CommentId(commentId)).orElse(null);
        if (target == null) return deletedTarget(action);
        if (target.comment().deleted() || target.postState() == PostState.DELETED) return deletedTarget(action);
        if (action == ReportAction.HIDE) {
            var hidden = hideComment.hide(new CommentId(commentId));
            if (hidden.comment().deleted() || hidden.postState() == PostState.DELETED) {
                return deletedTarget(action);
            }
            return new TargetVisibility(true, true);
        }
        return new TargetVisibility(true, target.comment().state() == CommentState.HIDDEN);
    }

    private TargetVisibility deletedTarget(ReportAction action) {
        if (action == ReportAction.HIDE) throw new DeletedReportTarget();
        return new TargetVisibility(false, null);
    }

    private record TargetVisibility(boolean available, Boolean hidden) {
    }
}
