package com.leets.blog.service;

import com.leets.blog.dto.report.ReportCreateRequest;
import com.leets.blog.dto.report.ReportResponse;
import com.leets.blog.entity.*;
import com.leets.blog.exception.PostApiException;
import com.leets.blog.repository.CommentRepository;
import com.leets.blog.repository.PostRepository;
import com.leets.blog.repository.ReportRepository;
import com.leets.blog.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("mysql")
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ReportResponse reportPost(Long postId, ReportCreateRequest request) {
        User reporter = findUser(request.userId());
        validateReason(request.reason());
        validateId(postId, "INVALID_POST_ID", "게시글");
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostApiException(404, "POST_NOT_FOUND", "존재하지 않는 게시글입니다."));
        validateReportablePost(post, reporter.getId());
        if (reportRepository.existsByReporterIdAndPostId(reporter.getId(), postId)) {
            throw duplicateReport();
        }
        return ReportResponse.from(reportRepository.saveAndFlush(Report.forPost(reporter, post, request.reason())));
    }

    public ReportResponse reportComment(Long commentId, ReportCreateRequest request) {
        User reporter = findUser(request.userId());
        validateReason(request.reason());
        validateId(commentId, "INVALID_COMMENT_ID", "댓글");
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new PostApiException(404, "COMMENT_NOT_FOUND", "존재하지 않거나 삭제된 댓글입니다."));
        validateReportablePost(comment.getPost(), reporter.getId());
        if (reportRepository.existsByReporterIdAndCommentId(reporter.getId(), commentId)) {
            throw duplicateReport();
        }
        return ReportResponse.from(reportRepository.saveAndFlush(Report.forComment(reporter, comment, request.reason())));
    }

    public ReportResponse resolve(Long reportId) {
        validateId(reportId, "INVALID_REPORT_ID", "신고");
        Report report = reportRepository.findForUpdateById(reportId)
                .orElseThrow(() -> new PostApiException(404, "REPORT_NOT_FOUND", "존재하지 않는 신고입니다."));
        if (report.getStatus() == ReportStatus.RESOLVED) {
            throw new PostApiException(409, "REPORT_ALREADY_RESOLVED", "이미 처리된 신고입니다.");
        }
        report.resolve();
        reportRepository.flush();
        return ReportResponse.from(report);
    }

    private User findUser(Long userId) {
        validateId(userId, "INVALID_INPUT", "사용자");
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new PostApiException(404, "MEMBER_NOT_FOUND", "존재하지 않는 사용자입니다."));
    }

    private void validateReportablePost(Post post, Long userId) {
        if (post.getDeletedAt() != null) {
            throw new PostApiException(404, "POST_DELETED", "삭제된 게시글입니다.");
        }
        if (post.getStatus() == PostStatus.HIDDEN) {
            throw new PostApiException(403, "POST_HIDDEN", "숨김 처리된 게시글입니다.");
        }
        if (post.isPrivate() && !Objects.equals(post.getUser().getId(), userId)) {
            throw new PostApiException(403, "POST_ACCESS_DENIED", "해당 게시글에 접근할 권한이 없습니다.");
        }
    }

    private void validateReason(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new PostApiException(400, "INVALID_INPUT", "신고 사유는 1자 이상 500자 이하로 입력해주세요.");
        }
    }

    private void validateId(Long id, String code, String target) {
        if (id == null || id <= 0) {
            throw new PostApiException(400, code, "올바른 " + target + " ID를 입력해주세요.");
        }
    }

    private PostApiException duplicateReport() {
        return new PostApiException(409, "DUPLICATE_REPORT", "이미 신고한 대상입니다.");
    }
}
