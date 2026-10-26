package com.example.demo.report.service;

import com.example.demo.comment.entity.Comment;
import com.example.demo.comment.repository.CommentRepository;
import com.example.demo.global.apiPayload.exception.GeneralException;
import com.example.demo.global.enums.RestrictionStatus;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.post.entity.Post;
import com.example.demo.post.exception.PostErrorCode;
import com.example.demo.post.repository.PostRepository;
import com.example.demo.report.converter.ReportConverter;
import com.example.demo.report.dto.ReportReqDTO;
import com.example.demo.report.dto.ReportResDTO;
import com.example.demo.report.entity.ReportComment;
import com.example.demo.report.entity.ReportCommentId;
import com.example.demo.report.entity.ReportPost;
import com.example.demo.report.entity.ReportPostId;
import com.example.demo.report.enums.ReportStatus;
import com.example.demo.report.exception.ReportErrorCode;
import com.example.demo.report.repository.ReportCommentRepository;
import com.example.demo.report.repository.ReportPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportPostRepository reportPostRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ReportConverter reportConverter;

    @Transactional
    public ReportResDTO.PostReportResponse postReport(Long postId,ReportReqDTO.PostReportRequest request){

        ReportPostId reportPostId = new ReportPostId(postId,request.reporterId());

        if (reportPostRepository.existsById(reportPostId)) {
            throw new GeneralException(ReportErrorCode.ALREADY_REPORTED);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new GeneralException(PostErrorCode.POST_NOT_FOUND));

        Member reporter = memberRepository.findById(request.reporterId())
                .orElseThrow(() ->
                        new GeneralException(PostErrorCode.MEMBER_NOT_FOUND));


        ReportPost reportPost = ReportPost.builder()
                .id(reportPostId)
                .post(post)
                .reporter(reporter)
                .reason(request.reason())
                .build();

        reportPostRepository.save(reportPost);


        return reportConverter.toReportPostCreateResponse(reportPost);
    }

    @Transactional
    public ReportResDTO.CommentReportResponse commentReport(Long commentId,ReportReqDTO.CommentReportRequest request){

        ReportCommentId reportCommentId = new ReportCommentId(commentId,request.reporterId());

        if (reportCommentRepository.existsById(reportCommentId)) {
            throw new GeneralException(ReportErrorCode.ALREADY_REPORTED);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new GeneralException(PostErrorCode.POST_NOT_FOUND));

        Member reporter = memberRepository.findById(request.reporterId())
                .orElseThrow(() ->
                        new GeneralException(PostErrorCode.MEMBER_NOT_FOUND));


        ReportComment reportComment = ReportComment.builder()
                .id(reportCommentId)
                .comment(comment)
                .reporter(reporter)
                .reason(request.reason())
                .build();

        reportCommentRepository.save(reportComment);


        return reportConverter.toReportCommentCreateResponse(reportComment);
    }

    @Transactional
    public ReportResDTO.PostReportStatusResponse updateReportStatus(
            Long postId,Long reporterId,
            ReportStatus status) {

        ReportPostId reportPostId = new ReportPostId(postId,reporterId);

        ReportPost reportPost = reportPostRepository.findById(reportPostId)
                .orElseThrow(() ->
                        new GeneralException(ReportErrorCode.REPORT_NOT_FOUND));

        if (status == ReportStatus.ACCEPTED) {
            Post post = reportPost.getPost();
            post.updateRestrictionStatus(RestrictionStatus.RESTRICTED);
        }

        reportPost.updateStatus(status);

        return reportConverter.toReportPostStatusUpdateResponse(reportPost,status);
    }
}
