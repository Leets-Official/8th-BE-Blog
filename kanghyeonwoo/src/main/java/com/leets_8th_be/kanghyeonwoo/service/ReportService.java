package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.PageResponse;
import com.leets_8th_be.kanghyeonwoo.dto.ReportResponse;
import com.leets_8th_be.kanghyeonwoo.dto.ReportResolveRequest;
import com.leets_8th_be.kanghyeonwoo.dto.ReportResolveResponse;
import java.time.LocalDateTime;
import java.time.Clock;
import com.leets_8th_be.kanghyeonwoo.util.Pagination;
import com.leets_8th_be.kanghyeonwoo.util.TextSearch;
import org.springframework.data.jpa.domain.Specification;
import com.leets_8th_be.kanghyeonwoo.dto.ReportCreateRequest;
import com.leets_8th_be.kanghyeonwoo.dto.ReportCreateResponse;
import com.leets_8th_be.kanghyeonwoo.entity.*;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import com.leets_8th_be.kanghyeonwoo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reports;
    private final PostRepository posts;
    private final UserRepository users;
    private final CommentRepository comments;
    private final Clock clock;

    @Transactional
    public ReportCreateResponse reportPost(Long postId, ReportCreateRequest request) {
        requirePositiveId(postId);
        User reporter = users.findById(request.reporterId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Post post = posts.findForUpdate(postId)
                .filter(p -> p.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        validateReporter(reporter, post.getUser(), ReportTargetType.POST, postId);
        return ReportCreateResponse.from(reports.saveAndFlush(Report.ofPost(reporter, post, request.reason())));
    }

    @Transactional
    public ReportCreateResponse reportComment(Long commentId, ReportCreateRequest request) {
        requirePositiveId(commentId);
        User reporter = users.findById(request.reporterId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Long postId = comments.findPostId(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        // All content writers lock the post before its comment, including post deletion.
        posts.findForUpdate(postId).filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        Comment comment = comments.findForUpdate(commentId)
                .filter(c -> c.getStatus() == CommentStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (comment.isDeleted()) throw new CustomException(ErrorCode.DELETED_COMMENT_REPORT);
        validateReporter(reporter, comment.getUser(), ReportTargetType.COMMENT, commentId);
        return ReportCreateResponse.from(reports.saveAndFlush(Report.ofComment(reporter, comment, request.reason())));
    }

    public PageResponse<ReportResponse> getReports(int page, int size, ReportStatus status,
                                                   ReportTargetType targetType, Long targetId, String keyword) {
        var pageable = Pagination.newestFirst(page, size);
        if (targetId != null) requirePositiveId(targetId);
        Specification<Report> criteria = (root, query, cb) -> cb.conjunction();
        if (status != null) criteria = criteria.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (targetType != null) criteria = criteria.and((root, query, cb) -> cb.equal(root.get("targetType"), targetType));
        if (targetId != null) criteria = criteria.and((root, query, cb) -> cb.equal(root.get("targetId"), targetId));
        if (keyword != null && !keyword.isBlank()) {
            String term = keyword.strip();
            criteria = criteria.and((root, query, cb) -> cb.or(
                    TextSearch.contains(cb, root.get("reason"), term),
                    TextSearch.contains(cb, root.get("targetTitleSnapshot"), term),
                    TextSearch.contains(cb, root.get("targetContentSnapshot"), term)));
        }
        return PageResponse.from(reports.findAll(criteria, pageable).map(ReportResponse::from));
    }

    @Transactional
    public ReportResolveResponse resolve(Long reportId, ReportResolveRequest request) {
        requirePositiveId(reportId);
        User processor = users.findById(request.processorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Report report = reports.findForUpdate(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        report.resolve(request.action(), request.resolutionReason(), processor, LocalDateTime.now(clock));
        TargetVisibility visibility = report.getTargetType() == ReportTargetType.POST
                ? processPost(report.getTargetId(), request.action())
                : processComment(report.getTargetId(), request.action());
        return ReportResolveResponse.from(report, visibility.available(), visibility.hidden());
    }

    private TargetVisibility processPost(Long postId, ReportResolutionAction action) {
        Post post = posts.findForUpdate(postId).orElse(null);
        if (post == null) return deletedTarget(action);
        if (action == ReportResolutionAction.HIDE) post.hide();
        return new TargetVisibility(true, post.getStatus() == PostStatus.HIDDEN);
    }

    private TargetVisibility processComment(Long commentId, ReportResolutionAction action) {
        Long postId = comments.findPostId(commentId).orElse(null);
        if (postId == null || posts.findForUpdate(postId).isEmpty()) return deletedTarget(action);
        Comment comment = comments.findForUpdate(commentId).orElse(null);
        if (comment == null || comment.isDeleted()) return deletedTarget(action);
        if (action == ReportResolutionAction.HIDE) comment.hide();
        return new TargetVisibility(true, comment.getStatus() == CommentStatus.HIDDEN);
    }

    private TargetVisibility deletedTarget(ReportResolutionAction action) {
        if (action == ReportResolutionAction.HIDE) throw new CustomException(ErrorCode.REPORT_TARGET_DELETED);
        return new TargetVisibility(false, null);
    }

    private record TargetVisibility(boolean available, Boolean hidden) {}

    private void validateReporter(User reporter, User author, ReportTargetType type, Long targetId) {
        if (reporter.getId().equals(author.getId())) throw new CustomException(ErrorCode.SELF_REPORT_FORBIDDEN);
        if (reports.existsByReporterIdAndTargetTypeAndTargetId(reporter.getId(), type, targetId)) {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }
    }

    private static void requirePositiveId(Long id) {
        if (id == null || id < 1) throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
