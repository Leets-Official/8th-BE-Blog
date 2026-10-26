package com.leets.blog.service;

import com.leets.blog.dto.report.ReportCreateRequest;
import com.leets.blog.dto.report.ReportResponse;
import com.leets.blog.entity.Member;
import com.leets.blog.entity.PostStatus;
import com.leets.blog.entity.Report;
import com.leets.blog.entity.ReportTargetType;
import com.leets.blog.global.exception.CustomException;
import com.leets.blog.global.exception.ErrorCode;
import com.leets.blog.repository.CommentRepository;
import com.leets.blog.repository.MemberRepository;
import com.leets.blog.repository.PostRepository;
import com.leets.blog.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReportResponse reportPost(Long postId, ReportCreateRequest request) {
        // 숨김 처리된 게시글은 없는 글로 보고 신고할 수 없다.
        postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return createReport(ReportTargetType.POST, postId, request);
    }

    @Transactional
    public ReportResponse reportComment(Long commentId, ReportCreateRequest request) {
        commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        return createReport(ReportTargetType.COMMENT, commentId, request);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        report.resolve();
        reportRepository.flush();

        return ReportResponse.from(report);
    }

    private ReportResponse createReport(ReportTargetType targetType, Long targetId, ReportCreateRequest request) {
        Member reporter = memberRepository.findById(request.reporterId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporter.getId(), targetType, targetId)) {
            throw new CustomException(ErrorCode.REPORT_DUPLICATED);
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason(request.reason())
                .build();

        // 같은 신고가 동시에 들어와 위 검사를 함께 통과하면, 유니크 제약 위반으로 한 번 더 막는다.
        try {
            reportRepository.saveAndFlush(report);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.REPORT_DUPLICATED);
        }

        return ReportResponse.from(report);
    }
}
