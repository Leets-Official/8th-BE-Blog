package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.request.ReportCreateRequest;
import com.example.demo.dto.response.ReportResponse;
import com.example.demo.exception.*;
import com.example.demo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final InteractionAccess access;
    private final ReportRepository reports;

    public ReportResponse reportPost(Long postId, Long userId, ReportCreateRequest request) {
        Member member = access.memberForUpdate(userId);
        access.validId(postId);
        if (reports.existsByReporterIdAndPostId(userId, postId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }
        Post post = access.readablePost(postId, userId);
        rejectSelfReport(post.getMember(), userId);
        return ReportResponse.from(reports.saveAndFlush(Report.forPost(member, post, request.reason())));
    }

    public ReportResponse reportComment(Long commentId, Long userId, ReportCreateRequest request) {
        Member member = access.memberForUpdate(userId);
        access.validId(commentId);
        if (reports.existsByReporterIdAndCommentId(userId, commentId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }
        Comment comment = access.readableComment(commentId, userId);
        rejectSelfReport(comment.getMember(), userId);
        return ReportResponse.from(reports.saveAndFlush(Report.forComment(member, comment, request.reason())));
    }

    private void rejectSelfReport(Member owner, Long userId) {
        if (owner.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_DENIED);
        }
    }
}
