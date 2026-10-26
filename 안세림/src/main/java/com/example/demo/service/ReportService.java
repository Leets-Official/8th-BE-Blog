package com.example.demo.service;

import com.example.demo.converter.ReportConverter;
import com.example.demo.dto.report.ReportCreateRequest;
import com.example.demo.dto.report.ReportResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.entity.Report;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ReportResponse reportPost(Long postId, ReportCreateRequest request) {
        Member reporter = findMember(request.reporterId());
        Post post = postRepository.findById(postId)
                .filter(p -> !p.isHidden())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (reportRepository.existsByReporterIdAndPostId(reporter.getId(), postId)) {
            throw new CustomException(ErrorCode.REPORT_DUPLICATED);
        }

        Report report = reportRepository.save(Report.ofPost(reporter, post, request.reason()));
        return ReportConverter.toResponse(report);
    }

    @Transactional
    public ReportResponse reportComment(Long commentId, ReportCreateRequest request) {
        Member reporter = findMember(request.reporterId());
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (reportRepository.existsByReporterIdAndCommentId(reporter.getId(), commentId)) {
            throw new CustomException(ErrorCode.REPORT_DUPLICATED);
        }

        Report report = reportRepository.save(Report.ofComment(reporter, comment, request.reason()));
        return ReportConverter.toResponse(report);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        report.resolve();
        return ReportConverter.toResponse(report);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}