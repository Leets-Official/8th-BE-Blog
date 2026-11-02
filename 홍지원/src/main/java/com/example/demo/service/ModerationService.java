package com.example.demo.service;

import com.example.demo.controller.ApiExceptionHandler.ApiException;
import com.example.demo.domain.comment.Comment;
import com.example.demo.domain.comment.CommentRepository;
import com.example.demo.domain.member.Member;
import com.example.demo.domain.member.MemberRepository;
import com.example.demo.domain.post.Post;
import com.example.demo.domain.post.PostRepository;
import com.example.demo.domain.post.PostState;
import com.example.demo.domain.post.PostStateRepository;
import com.example.demo.domain.post.PostVisibility;
import com.example.demo.domain.report.Report;
import com.example.demo.domain.report.ReportRepository;
import com.example.demo.domain.report.ReportStatus;
import com.example.demo.dto.ModerationDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ModerationService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostStateRepository postStateRepository;
    private final ReportRepository reportRepository;

    public ModerationService(PostRepository postRepository, CommentRepository commentRepository,
                             MemberRepository memberRepository, PostStateRepository postStateRepository,
                             ReportRepository reportRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postStateRepository = postStateRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ModerationDto.ReportView reportPost(long postId, String userId, ModerationDto.ReportRequest request) {
        Member reporter = currentMember(userId);
        Post post = findPost(postId);
        String reason = validateText(request == null ? null : request.reason(), "INVALID_REPORT_REASON",
                "신고 사유는 1~1000자여야 합니다.");
        if (reportRepository.existsByReporterIdAndPostId(reporter.getId(), post.getId())) {
            throw duplicateReport();
        }
        return saveReport(new Report(reporter, post, null, reason));
    }

    @Transactional
    public ModerationDto.ReportView reportComment(long commentId, String userId,
                                                   ModerationDto.ReportRequest request) {
        Member reporter = currentMember(userId);
        if (commentId < 1) throw new ApiException(400, "INVALID_COMMENT_ID", "댓글 ID는 1 이상이어야 합니다.");
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId).orElseThrow(
                () -> new ApiException(404, "COMMENT_NOT_FOUND", "해당 댓글을 찾을 수 없습니다."));
        if (comment.getPost().getDeletedAt() != null) {
            throw new ApiException(404, "POST_NOT_FOUND", "해당 게시글을 찾을 수 없습니다.");
        }
        String reason = validateText(request == null ? null : request.reason(), "INVALID_REPORT_REASON",
                "신고 사유는 1~1000자여야 합니다.");
        if (reportRepository.existsByReporterIdAndCommentId(reporter.getId(), comment.getId())) {
            throw duplicateReport();
        }
        return saveReport(new Report(reporter, null, comment, reason));
    }

    public List<ModerationDto.ReportView> listReports(ReportStatus status, String userId) {
        currentMember(userId);
        if (status == null) throw new ApiException(400, "INVALID_REPORT_STATUS", "신고 상태가 필요합니다.");
        return reportRepository.findAllByStatusOrderByCreatedAtAsc(status).stream()
                .map(ModerationDto.ReportView::from).toList();
    }

    @Transactional
    public ModerationDto.ReportView resolveReport(long reportId, String userId,
                                                   ModerationDto.ResolveRequest request) {
        Member resolver = currentMember(userId);
        if (reportId < 1) throw new ApiException(400, "INVALID_REPORT_ID", "신고 ID는 1 이상이어야 합니다.");
        Report report = reportRepository.findByIdForUpdate(reportId).orElseThrow(
                () -> new ApiException(404, "REPORT_NOT_FOUND", "해당 신고를 찾을 수 없습니다."));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ApiException(409, "INVALID_REPORT_TRANSITION", "대기 중인 신고만 처리할 수 있습니다.");
        }
        String result = validateText(request == null ? null : request.result(), "INVALID_REPORT_RESULT",
                "처리 결과는 1~1000자여야 합니다.");
        report.resolve(resolver, result);
        return ModerationDto.ReportView.from(report);
    }

    @Transactional
    public ModerationDto.State changeVisibility(long postId, String userId,
                                                ModerationDto.VisibilityRequest request) {
        currentMember(userId);
        Post post = lockPost(postId);
        if (request == null || request.status() == null) {
            throw new ApiException(400, "INVALID_VISIBILITY", "게시글 상태는 ACTIVE 또는 HIDDEN이어야 합니다.");
        }
        PostState state = getOrCreateState(post);
        if (state.getVisibility() == request.status()) {
            throw new ApiException(409, "INVALID_VISIBILITY_TRANSITION", "현재 상태와 다른 상태로만 변경할 수 있습니다.");
        }
        state.changeVisibility(request.status());
        return stateView(state);
    }

    @Transactional
    public ModerationDto.State acceptComment(long postId, long commentId, String userId) {
        Member member = currentMember(userId);
        Post post = lockPost(postId);
        checkAuthor(post, member);
        if (commentId < 1) throw new ApiException(400, "INVALID_COMMENT_ID", "댓글 ID는 1 이상이어야 합니다.");
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId).orElseThrow(
                () -> new ApiException(404, "COMMENT_NOT_FOUND", "해당 댓글을 찾을 수 없습니다."));
        if (!comment.getPost().getId().equals(post.getId())) {
            throw new ApiException(400, "COMMENT_POST_MISMATCH", "댓글이 해당 게시글에 속하지 않습니다.");
        }
        PostState state = getOrCreateState(post);
        if (state.getAcceptedComment() != null) {
            throw new ApiException(409, "COMMENT_ALREADY_ACCEPTED", "게시글에 이미 채택된 댓글이 있습니다.");
        }
        state.accept(comment);
        try {
            postStateRepository.saveAndFlush(state);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(409, "COMMENT_ALREADY_ACCEPTED", "해당 댓글은 이미 다른 게시글에서 채택되었습니다.");
        }
        return stateView(state);
    }

    @Transactional
    public ModerationDto.State unacceptComment(long postId, String userId) {
        Member member = currentMember(userId);
        Post post = lockPost(postId);
        checkAuthor(post, member);
        PostState state = postStateRepository.findByPostId(post.getId()).orElseThrow(
                () -> new ApiException(409, "NO_ACCEPTED_COMMENT", "게시글에 채택된 댓글이 없습니다."));
        if (state.getAcceptedComment() == null) {
            throw new ApiException(409, "NO_ACCEPTED_COMMENT", "게시글에 채택된 댓글이 없습니다.");
        }
        state.clearAcceptedComment();
        return stateView(state);
    }

    private ModerationDto.ReportView saveReport(Report report) {
        try {
            return ModerationDto.ReportView.from(reportRepository.saveAndFlush(report));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateReport();
        }
    }

    private Post findPost(long id) {
        if (id < 1) throw new ApiException(400, "INVALID_POST_ID", "게시글 ID는 1 이상이어야 합니다.");
        return postRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new ApiException(404, "POST_NOT_FOUND", "해당 게시글을 찾을 수 없습니다."));
    }

    private Post lockPost(long id) {
        if (id < 1) throw new ApiException(400, "INVALID_POST_ID", "게시글 ID는 1 이상이어야 합니다.");
        return postRepository.findActiveByIdForUpdate(id).orElseThrow(
                () -> new ApiException(404, "POST_NOT_FOUND", "해당 게시글을 찾을 수 없습니다."));
    }

    private Member currentMember(String userId) {
        long id;
        try {
            id = Long.parseLong(userId);
        } catch (RuntimeException exception) {
            throw new ApiException(401, "UNAUTHORIZED", "유효한 사용자 ID가 필요합니다.");
        }
        return memberRepository.findById(id).filter(m -> m.getDeletedAt() == null).orElseThrow(
                () -> new ApiException(401, "UNAUTHORIZED", "유효한 사용자 ID가 필요합니다."));
    }

    private PostState getOrCreateState(Post post) {
        return postStateRepository.findByPostId(post.getId()).orElseGet(
                () -> postStateRepository.save(new PostState(post)));
    }

    private ModerationDto.State stateView(PostState state) {
        return new ModerationDto.State(state.getPost().getId(), state.getVisibility(),
                state.getAcceptedComment() == null ? null : state.getAcceptedComment().getId());
    }

    private void checkAuthor(Post post, Member member) {
        if (!post.getMember().getId().equals(member.getId())) {
            throw new ApiException(403, "POST_ACCESS_DENIED", "게시글 작성자만 댓글을 채택하거나 해제할 수 있습니다.");
        }
    }

    private String validateText(String value, String code, String message) {
        if (value == null || value.isBlank() || value.length() > 1000) {
            throw new ApiException(400, code, message);
        }
        return value.trim();
    }

    private ApiException duplicateReport() {
        return new ApiException(409, "DUPLICATE_REPORT", "같은 사용자는 같은 대상을 중복 신고할 수 없습니다.");
    }
}
