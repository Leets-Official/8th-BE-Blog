package com.example.demo;

import com.example.demo.controller.*;
import com.example.demo.domain.*;
import com.example.demo.dto.request.ReportCreateRequest;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@org.springframework.test.context.ActiveProfiles("test")
@Import({InteractionAccess.class, LikeService.class, ReportService.class})
class InteractionIntegrationTests {
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
    @Autowired TestEntityManager em;
    @Autowired LikeService likes;
    @Autowired ReportService reports;
    @Autowired PostLikeRepository postLikes;
    @Autowired CommentLikeRepository commentLikes;
    @Autowired ReportRepository reportRepository;
    Member owner;
    Member actor;
    Post post;
    Comment comment;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        new org.springframework.transaction.support.TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
        owner = member("owner");
        actor = member("actor");
        post = em.persistAndFlush(new Post("title", "content", owner, false));
        comment = new Comment();
        ReflectionTestUtils.setField(comment, "member", owner);
        ReflectionTestUtils.setField(comment, "post", post);
        ReflectionTestUtils.setField(comment, "content", "comment");
        em.persistAndFlush(comment);
        });
        mvc = MockMvcBuilders.standaloneSetup(new LikeController(likes), new ReportController(reports))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    Member member(String name) {
        Member member = new Member();
        ReflectionTestUtils.setField(member, "name", name);
        return em.persistAndFlush(member);
    }

    ReportCreateRequest reason() {
        return new ReportCreateRequest("spam");
    }

    void fails(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(code));
    }

    @Test
    void postLikesAreIdempotentAndCanBeRegisteredAgain() {
        assertThat(likes.likePost(post.getId(), actor.getId()).likeCount()).isEqualTo(1);
        assertThat(likes.likePost(post.getId(), actor.getId()).likeCount()).isEqualTo(1);
        likes.unlikePost(post.getId(), actor.getId());
        likes.unlikePost(post.getId(), actor.getId());
        assertThat(postLikes.countByPostId(post.getId())).isZero();
        assertThat(likes.likePost(post.getId(), actor.getId()).liked()).isTrue();
    }

    @Test
    void commentLikesAreIdempotentAndOwnerCanLikeOwnComment() {
        likes.likeComment(comment.getId(), owner.getId());
        likes.likeComment(comment.getId(), owner.getId());
        assertThat(commentLikes.countByCommentId(comment.getId())).isEqualTo(1);
        likes.unlikeComment(comment.getId(), owner.getId());
        likes.unlikeComment(comment.getId(), owner.getId());
        assertThat(commentLikes.count()).isZero();
    }

    @Test
    void cancellationIsAllowedAfterTargetsBecomeUnavailable() {
        likes.likePost(post.getId(), actor.getId());
        likes.likeComment(comment.getId(), actor.getId());
        post.hide();
        post.softDelete();
        comment.hide();
        comment.softDelete();
        likes.unlikePost(post.getId(), actor.getId());
        likes.unlikeComment(comment.getId(), actor.getId());
        assertThat(postLikes.count()).isZero();
        assertThat(commentLikes.count()).isZero();
    }

    @Test
    void deletedMembersCannotInteract() {
        actor.softDelete();
        em.flush();
        fails(() -> likes.likePost(post.getId(), actor.getId()), ErrorCode.MEMBER_NOT_FOUND);
        fails(() -> reports.reportComment(comment.getId(), actor.getId(), reason()), ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void privatePostAccessAppliesToCommentsToo() {
        ReflectionTestUtils.setField(post, "isPrivate", true);
        fails(() -> likes.likeComment(comment.getId(), actor.getId()), ErrorCode.POST_ACCESS_DENIED);
        fails(() -> reports.reportComment(comment.getId(), actor.getId(), reason()), ErrorCode.POST_ACCESS_DENIED);
        assertThat(likes.likePost(post.getId(), owner.getId()).liked()).isTrue();
    }

    @Test
    void hiddenAndDeletedTargetsRejectNewInteractions() {
        comment.hide();
        fails(() -> likes.likeComment(comment.getId(), actor.getId()), ErrorCode.COMMENT_ACCESS_DENIED);
        post.softDelete();
        fails(() -> reports.reportPost(post.getId(), actor.getId(), reason()), ErrorCode.POST_DELETED);
    }

    @Test
    void ownTargetsCannotBeReported() {
        fails(() -> reports.reportPost(post.getId(), owner.getId(), reason()), ErrorCode.SELF_REPORT_DENIED);
        fails(() -> reports.reportComment(comment.getId(), owner.getId(), reason()), ErrorCode.SELF_REPORT_DENIED);
    }

    @Test
    void postAndCommentReportsAreIndependentAndDuplicatesConflict() {
        assertThat(reports.reportPost(post.getId(), actor.getId(), reason()).status()).isEqualTo(ReportStatus.PENDING);
        reports.reportComment(comment.getId(), actor.getId(), reason());
        fails(() -> reports.reportPost(post.getId(), actor.getId(), reason()), ErrorCode.DUPLICATE_REPORT);
        fails(() -> reports.reportComment(comment.getId(), actor.getId(), reason()), ErrorCode.DUPLICATE_REPORT);
        assertThat(reportRepository.count()).isEqualTo(2);
    }

    @Test
    void approvalPersistsStatusAndHiddenTargetAndCanBeRepeated() {
        Long id = reports.reportPost(post.getId(), actor.getId(), reason()).id();
        assertThat(reports.approve(id, owner.getId()).targetHidden()).isTrue();
        assertThat(reports.approve(id, owner.getId()).status()).isEqualTo(ReportStatus.APPROVED);
        em.flush();
        em.clear();
        Report persisted = reportRepository.findById(id).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(persisted.getPost().isHidden()).isTrue();
    }

    @Test
    void commentApprovalDoesNotHideRepliesOrChangeOtherReports() {
        Comment reply = new Comment();
        ReflectionTestUtils.setField(reply, "member", actor);
        ReflectionTestUtils.setField(reply, "post", post);
        ReflectionTestUtils.setField(reply, "parent", comment);
        ReflectionTestUtils.setField(reply, "content", "reply");
        em.persistAndFlush(reply);
        Member other = member("other");
        Long first = reports.reportComment(comment.getId(), actor.getId(), reason()).id();
        Long second = reports.reportComment(comment.getId(), other.getId(), reason()).id();
        reports.approve(first, owner.getId());
        assertThat(comment.isHidden()).isTrue();
        assertThat(reply.isHidden()).isFalse();
        assertThat(post.isHidden()).isFalse();
        assertThat(reportRepository.findById(second).orElseThrow().getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(reports.approve(second, owner.getId()).status()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    void rejectedReportsCannotBeApprovedOrReportedAgain() {
        Long id = reports.reportPost(post.getId(), actor.getId(), reason()).id();
        ReflectionTestUtils.setField(reportRepository.findById(id).orElseThrow(), "status", ReportStatus.REJECTED);
        fails(() -> reports.approve(id, owner.getId()), ErrorCode.REPORT_STATE_CONFLICT);
        fails(() -> reports.reportPost(post.getId(), actor.getId(), reason()), ErrorCode.DUPLICATE_REPORT);
        assertThat(post.isHidden()).isFalse();
    }

    @Test
    void databaseRejectsDuplicateLikes() {
        postLikes.saveAndFlush(new PostLike(actor, post));
        assertThatThrownBy(() -> postLikes.saveAndFlush(new PostLike(actor, post)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsDuplicateReports() {
        reportRepository.saveAndFlush(Report.forPost(actor, post, "first"));
        assertThatThrownBy(() -> reportRepository.saveAndFlush(Report.forPost(actor, post, "second")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void httpValidatesHeadersAndReportReason() throws Exception {
        mvc.perform(post("/posts/{id}/likes", post.getId())).andExpect(status().isBadRequest());
        for (String json : new String[]{"{}", "{\"reason\":\"   \"}",
                "{\"reason\":\"" + "a".repeat(256) + "\"}"}) {
            mvc.perform(post("/comments/{id}/reports", comment.getId()).header("X-User-Id", actor.getId())
                    .contentType("application/json").content(json)).andExpect(status().isBadRequest());
        }
    }

    @Test
    void httpExposesLikeReportAndApprovalFlow() throws Exception {
        mvc.perform(post("/posts/{id}/likes", post.getId()).header("X-User-Id", actor.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.likeCount").value(1));
        mvc.perform(delete("/posts/{id}/likes", post.getId()).header("X-User-Id", actor.getId()))
                .andExpect(status().isNoContent());
        mvc.perform(post("/comments/{id}/reports", comment.getId()).header("X-User-Id", actor.getId())
                .contentType("application/json").content("{\"reason\":\"spam\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"));
        Long id = reportRepository.findAll().getFirst().getId();
        mvc.perform(post("/reports/{id}/approve", id).header("X-User-Id", owner.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.targetHidden").value(true));
        mvc.perform(post("/comments/{id}/reports", comment.getId()).header("X-User-Id", actor.getId())
                .contentType("application/json").content("{\"reason\":\"spam\"}"))
                .andExpect(status().isConflict());
    }
    @Test
    @org.springframework.transaction.annotation.Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void concurrentRequestsDoNotDuplicateLikesReportsOrApproval() throws Exception {
        var tx = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var likesStart = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Long> like = () -> {
                likesStart.await();
                return likes.likePost(post.getId(), actor.getId()).likeCount();
            };
            var firstLike = pool.submit(like);
            var secondLike = pool.submit(like);
            likesStart.countDown();
            assertThat(firstLike.get(10, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(secondLike.get(10, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(1);

            var reportsStart = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<String> report = () -> {
                reportsStart.await();
                try {
                    reports.reportPost(post.getId(), actor.getId(), reason());
                    return "CREATED";
                } catch (BusinessException error) {
                    return error.getErrorCode().name();
                }
            };
            var firstReport = pool.submit(report);
            var secondReport = pool.submit(report);
            reportsStart.countDown();
            assertThat(java.util.List.of(firstReport.get(10, java.util.concurrent.TimeUnit.SECONDS),
                    secondReport.get(10, java.util.concurrent.TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("CREATED", "DUPLICATE_REPORT");
            Long reportId = tx.execute(status -> reportRepository.findAll().getFirst().getId());

            var approvalStart = new java.util.concurrent.CountDownLatch(1);
            var firstApproval = pool.submit(() -> {
                approvalStart.await();
                return reports.approve(reportId, owner.getId()).status();
            });
            var secondApproval = pool.submit(() -> {
                approvalStart.await();
                return reports.approve(reportId, actor.getId()).status();
            });
            approvalStart.countDown();
            assertThat(firstApproval.get(10, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(ReportStatus.APPROVED);
            assertThat(secondApproval.get(10, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(ReportStatus.APPROVED);
            tx.executeWithoutResult(status -> {
                Report persisted = reportRepository.findById(reportId).orElseThrow();
                assertThat(persisted.getPost().isHidden()).isTrue();
                assertThat(postLikes.countByPostId(post.getId())).isEqualTo(1);
            });
        } finally {
            pool.shutdownNow();
            tx.executeWithoutResult(status -> {
                reportRepository.deleteAllInBatch();
                postLikes.deleteAllInBatch();
                em.getEntityManager().createQuery("delete from Comment").executeUpdate();
                em.getEntityManager().createQuery("delete from Post").executeUpdate();
                em.getEntityManager().createQuery("delete from Member").executeUpdate();
            });
        }
    }

}
