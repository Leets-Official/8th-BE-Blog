package com.leets.mission.service;

import com.leets.mission.domain.comment.Comment;
import com.leets.mission.domain.comment.CommentRepository;
import com.leets.mission.domain.comment.CommentStatus;
import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRole;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.domain.post.Post;
import com.leets.mission.domain.post.PostRepository;
import com.leets.mission.domain.post.PostStatus;
import com.leets.mission.domain.report.Report;
import com.leets.mission.domain.report.ReportRepository;
import com.leets.mission.dto.report.ReportCreateRequest;
import com.leets.mission.dto.report.ReportResponse;
import com.leets.mission.domain.report.ReportStatus;
import com.leets.mission.dto.report.ReportDecisionRequest;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReportResponse createReport(
            ReportCreateRequest request,
            Long reporterId
    ) {
        if (reporterId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        Post post = null;
        Comment comment = null;

        if (request.postId() != null) {
            post = postRepository
                    .findByIdAndStatusAndDeletedAtIsNull(
                            request.postId(),
                            PostStatus.ACTIVE
                    )
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

            if (post.getMember().getId().equals(reporterId)) {
                throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

            if (reportRepository.existsByReporterIdAndPostId(
                    reporterId,
                    post.getId()
            )) {
                throw new CustomException(ErrorCode.DUPLICATE_REPORT);
            }
        } else {
            comment = commentRepository
                    .findByIdAndStatusAndDeletedAtIsNull(
                            request.commentId(),
                            CommentStatus.ACTIVE
                    )
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            if (comment.getMember().getId().equals(reporterId)) {
                throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

            if (reportRepository.existsByReporterIdAndCommentId(
                    reporterId,
                    comment.getId()
            )) {
                throw new CustomException(ErrorCode.DUPLICATE_REPORT);
            }
        }

        Report report = Report.builder()
                .reporter(reporter)
                .post(post)
                .comment(comment)
                .reason(request.reason())
                .build();

        return ReportResponse.from(reportRepository.save(report));
    }

    public Page<ReportResponse> getReports(
            Long adminId,
            ReportStatus status,
            Pageable pageable
    ) {
        validateAdmin(adminId);

        return reportRepository.findAllByStatus(status, pageable)
                .map(ReportResponse::from);
    }

    private void validateAdmin(Long adminId) {
        if (adminId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        if (admin.getRole() != MemberRole.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ACCESS_DENIED);
        }
    }

    @Transactional
    public ReportResponse processReport(
            Long reportId,
            ReportDecisionRequest request,
            Long adminId
    ) {
        validateAdmin(adminId);

        Report report = reportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        if (request.status() == ReportStatus.RESOLVED) {
            hideReportedContent(report);
            report.resolve();
        } else {
            report.dismiss();
        }

        return ReportResponse.from(report);
    }

    private void hideReportedContent(Report report) {
        if (report.getPost() != null) {
            Post post = report.getPost();

            if (post.getDeletedAt() != null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND);
            }

            if (post.getStatus() == PostStatus.HIDDEN) {
                throw new CustomException(ErrorCode.POST_ALREADY_HIDDEN);
            }

            post.hide();
            return;
        }

        Comment comment = report.getComment();

        if (comment.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (comment.getStatus() == CommentStatus.HIDDEN) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_HIDDEN);
        }

        comment.hide();
    }

}