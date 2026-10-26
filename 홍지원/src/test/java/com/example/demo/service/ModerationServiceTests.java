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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTests {
    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    @Mock MemberRepository memberRepository;
    @Mock PostStateRepository postStateRepository;
    @Mock ReportRepository reportRepository;

    private ModerationService service;
    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        service = new ModerationService(postRepository, commentRepository, memberRepository,
                postStateRepository, reportRepository);
        member = new Member("Reporter", (byte) 20, "reporter@example.com", "password");
        ReflectionTestUtils.setField(member, "id", 1L);
        post = new Post(member, "Title", "Content");
        ReflectionTestUtils.setField(post, "id", 10L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    }

    @Test
    void createsPostReportWithPendingStatusAndTrimmedReason() {
        when(postRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(post));
        when(reportRepository.existsByReporterIdAndPostId(1L, 10L)).thenReturn(false);
        when(reportRepository.saveAndFlush(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });

        ModerationDto.ReportView result = service.reportPost(10L, "1",
                new ModerationDto.ReportRequest("  inappropriate  "));

        assertEquals(7L, result.reportId());
        assertEquals("POST", result.targetType());
        assertEquals("inappropriate", result.reason());
        assertEquals(ReportStatus.PENDING, result.status());
    }

    @Test
    void rejectsDuplicateReportBySameMember() {
        when(postRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(post));
        when(reportRepository.existsByReporterIdAndPostId(1L, 10L)).thenReturn(true);

        assertThrows(ApiException.class, () -> service.reportPost(10L, "1",
                new ModerationDto.ReportRequest("duplicate")));
    }

    @Test
    void rejectsDuplicateCommentReportBySameMember() {
        Comment comment = new Comment(member, post, "comment");
        ReflectionTestUtils.setField(comment, "id", 21L);
        when(commentRepository.findByIdAndDeletedAtIsNull(21L)).thenReturn(Optional.of(comment));
        when(reportRepository.existsByReporterIdAndCommentId(1L, 21L)).thenReturn(true);

        assertThrows(ApiException.class, () -> service.reportComment(21L, "1",
                new ModerationDto.ReportRequest("duplicate")));
    }

    @Test
    void resolvesPendingReportAndRejectsSecondResolution() {
        Report report = new Report(member, post, null, "reason");
        ReflectionTestUtils.setField(report, "id", 5L);
        when(reportRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(report));

        service.resolveReport(5L, "1", new ModerationDto.ResolveRequest("reviewed"));

        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertEquals("reviewed", report.getResult());
        assertThrows(ApiException.class, () -> service.resolveReport(5L, "1",
                new ModerationDto.ResolveRequest("again")));
    }

    @Test
    void changesVisibilityAndRejectsInvalidRepeatedTransition() {
        when(postRepository.findActiveByIdForUpdate(10L)).thenReturn(Optional.of(post));
        PostState state = new PostState(post);
        when(postStateRepository.findByPostId(10L)).thenReturn(Optional.<PostState>empty(), Optional.of(state));
        when(postStateRepository.save(any(PostState.class))).thenReturn(state);

        ModerationDto.State result = service.changeVisibility(10L, "1",
                new ModerationDto.VisibilityRequest(PostVisibility.HIDDEN));

        assertEquals(10L, result.postId());
        assertEquals(PostVisibility.HIDDEN, result.status());
        assertThrows(ApiException.class, () -> service.changeVisibility(10L, "1",
                new ModerationDto.VisibilityRequest(PostVisibility.HIDDEN)));
    }

    @Test
    void acceptsOnlyOneCommentForOwnedPost() {
        Comment comment = new Comment(member, post, "answer");
        ReflectionTestUtils.setField(comment, "id", 21L);
        PostState state = new PostState(post);
        when(postRepository.findActiveByIdForUpdate(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndDeletedAtIsNull(21L)).thenReturn(Optional.of(comment));
        when(postStateRepository.findByPostId(10L)).thenReturn(Optional.of(state));

        ModerationDto.State result = service.acceptComment(10L, 21L, "1");

        assertEquals(21L, result.acceptedCommentId());
        verify(postStateRepository).saveAndFlush(state);
        assertThrows(ApiException.class, () -> service.acceptComment(10L, 21L, "1"));
    }

    @Test
    void rejectsCommentFromAnotherPost() {
        Post anotherPost = new Post(member, "Other", "Content");
        ReflectionTestUtils.setField(anotherPost, "id", 11L);
        Comment comment = new Comment(member, anotherPost, "answer");
        ReflectionTestUtils.setField(comment, "id", 22L);
        when(postRepository.findActiveByIdForUpdate(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndDeletedAtIsNull(22L)).thenReturn(Optional.of(comment));

        assertThrows(ApiException.class, () -> service.acceptComment(10L, 22L, "1"));
    }
}
