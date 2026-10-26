package com.leets.mission.domain.report.service;

import com.leets.mission.domain.comment.entity.Comment;
import com.leets.mission.domain.comment.repository.CommentRepository;
import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.post.repository.PostRepository;
import com.leets.mission.domain.report.converter.ReportConverter;
import com.leets.mission.domain.report.dto.ReportRequestDTO;
import com.leets.mission.domain.report.dto.ReportResponseDTO;
import com.leets.mission.domain.report.entity.Report;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.domain.report.repository.ReportRepository;
import com.leets.mission.domain.user.entity.User;
import com.leets.mission.domain.user.repository.UserRepository;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
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
    private final UserRepository userRepository;

    // 게시글 신고
    @Transactional
    public ReportResponseDTO.DetailDTO reportPost(Long postId, ReportRequestDTO.CreateDTO request) {
        Post post = findActivePost(postId);
        User reporter = findUser(request.userId());
        if (post.isWrittenBy(reporter.getId())) {
            //본인 글 신고 불가
            throw new GeneralException(ErrorStatus.REPORT_400_1);
        }
        return createReport(request, reporter, ReportTargetType.POST, postId);
    }

    // 댓글 신고
    @Transactional
    public ReportResponseDTO.DetailDTO reportComment(Long commentId, ReportRequestDTO.CreateDTO request) {
        Comment comment = findActiveComment(commentId);
        User reporter = findUser(request.userId());
        if (comment.isWrittenBy(reporter.getId())) {
            //본인 댓글 신고 불가
            throw new GeneralException(ErrorStatus.REPORT_400_1);
        }
        return createReport(request, reporter, ReportTargetType.COMMENT, commentId);
    }

    // 신고 처리 (PENDING → RESOLVED/REJECTED), 승인 시 신고 대상을 숨김
    @Transactional
    public ReportResponseDTO.DetailDTO processReport(Long reportId, ReportRequestDTO.ProcessDTO request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REPORT_404_1));
        report.process(request.status()); //이미 처리된 신고면 409
        if (report.isResolved()) {
            hideTarget(report);
        }
        return ReportConverter.toDetailDTO(report);
    }

    // 신고 대상이 남아 있고 아직 노출 중일 때만 숨김 (이미 숨김/삭제된 대상은 그대로 둠)
    private void hideTarget(Report report) {
        switch (report.getTargetType()) {
            case POST -> postRepository.findById(report.getTargetId())
                    .filter(post -> !post.isHidden())
                    .ifPresent(Post::hide);
            case COMMENT -> commentRepository.findById(report.getTargetId())
                    .filter(comment -> !comment.isHidden())
                    .ifPresent(Comment::hide);
        }
    }

    // 같은 사용자가 같은 대상을 이미 신고했으면 409
    private ReportResponseDTO.DetailDTO createReport(ReportRequestDTO.CreateDTO request, User reporter,
                                                     ReportTargetType targetType, Long targetId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, targetType, targetId)) {
            throw new GeneralException(ErrorStatus.REPORT_409_1);
        }
        try {
            // 동시 요청이 위 확인을 함께 통과해도 DB Unique 제약조건이 막도록 바로 INSERT
            Report report = reportRepository.saveAndFlush(ReportConverter.toReport(request, reporter, targetType, targetId));
            return ReportConverter.toDetailDTO(report);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.REPORT_409_1);
        }
    }

    // 숨김 게시글은 없는 게시글과 같이 취급 (404)
    private Post findActivePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_404_1));
        if (post.isHidden()) {
            throw new GeneralException(ErrorStatus.POST_404_1);
        }
        return post;
    }

    // 숨김 댓글, 숨김 게시글의 댓글은 없는 댓글과 같이 취급 (404)
    private Comment findActiveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_404_1));
        if (comment.isHidden() || comment.getPost().isHidden()) {
            throw new GeneralException(ErrorStatus.COMMENT_404_1);
        }
        return comment;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_404_1));
    }
}
