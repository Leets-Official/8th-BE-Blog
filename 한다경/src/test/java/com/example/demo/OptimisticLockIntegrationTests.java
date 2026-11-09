package com.example.demo;

import com.example.demo.domain.*;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.request.ReportCreateRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.service.InteractionAccess;
import com.example.demo.service.PostService;
import com.example.demo.service.ReportService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({InteractionAccess.class, ReportService.class, PostService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OptimisticLockIntegrationTests {
    @PersistenceContext EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired PostService posts;
    @Autowired ReportService reports;
    TransactionTemplate tx;
    TransactionTemplate independent;
    Long ownerId;
    Long actorId;
    Long postId;
    Long commentId;
    Long postReportId;
    Long commentReportId;

    @BeforeEach
    void setup() {
        tx = new TransactionTemplate(transactionManager);
        independent = new TransactionTemplate(transactionManager);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(status -> {
            Member owner = member("owner");
            Member actor = member("actor");
            Post post = new Post("original", "content", owner, false);
            em.persist(post);
            Comment comment = new Comment();
            ReflectionTestUtils.setField(comment, "member", owner);
            ReflectionTestUtils.setField(comment, "post", post);
            ReflectionTestUtils.setField(comment, "content", "comment");
            em.persist(comment);
            em.flush();
            ownerId = owner.getId();
            actorId = actor.getId();
            postId = post.getId();
            commentId = comment.getId();
        });
        postReportId = reports.reportPost(postId, actorId, new ReportCreateRequest("spam")).id();
        commentReportId = reports.reportComment(commentId, actorId, new ReportCreateRequest("spam")).id();
    }

    Member member(String name) {
        Member member = new Member();
        ReflectionTestUtils.setField(member, "name", name);
        em.persist(member);
        return member;
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            em.createQuery("delete from Report").executeUpdate();
            em.createQuery("delete from Comment").executeUpdate();
            em.createQuery("delete from Post").executeUpdate();
            em.createQuery("delete from Member").executeUpdate();
        });
    }

    @Test
    void staleEditCannotUndoPostApproval() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            em.find(Post.class, postId);
            independent.executeWithoutResult(other -> reports.approve(postReportId, actorId));
            posts.updatePost(postId, ownerId, new PostUpdateRequest("stale edit", "changed"));
        })).isInstanceOf(OptimisticLockingFailureException.class);
        tx.executeWithoutResult(status -> {
            Post post = em.find(Post.class, postId);
            assertThat(post.isHidden()).isTrue();
            assertThat(post.getTitle()).isEqualTo("original");
            assertThat(em.find(Report.class, postReportId).getStatus()).isEqualTo(ReportStatus.APPROVED);
        });
    }

    @Test
    void staleDeletionCannotUndoPostApproval() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            em.find(Post.class, postId);
            independent.executeWithoutResult(other -> reports.approve(postReportId, actorId));
            posts.deletePost(postId, ownerId);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        tx.executeWithoutResult(status -> {
            Post post = em.find(Post.class, postId);
            assertThat(post.isHidden()).isTrue();
            assertThat(post.isDeleted()).isFalse();
        });
    }

    @Test
    void staleApprovalRollsBackReportAndPreservesEditedPost() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            em.find(Post.class, postId);
            independent.executeWithoutResult(other ->
                    posts.updatePost(postId, ownerId, new PostUpdateRequest("new title", "new content")));
            reports.approve(postReportId, actorId);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        tx.executeWithoutResult(status -> {
            Post post = em.find(Post.class, postId);
            assertThat(post.getTitle()).isEqualTo("new title");
            assertThat(post.isHidden()).isFalse();
            assertThat(em.find(Report.class, postReportId).getStatus()).isEqualTo(ReportStatus.PENDING);
        });
        assertThat(reports.approve(postReportId, actorId).targetHidden()).isTrue();
    }

    @Test
    void staleApprovalCannotUndoPostDeletion() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            em.find(Post.class, postId);
            independent.executeWithoutResult(other -> posts.deletePost(postId, ownerId));
            reports.approve(postReportId, actorId);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        tx.executeWithoutResult(status -> {
            assertThat(em.find(Post.class, postId).isDeleted()).isTrue();
            assertThat(em.find(Report.class, postReportId).getStatus()).isEqualTo(ReportStatus.PENDING);
        });
    }

    @Test
    void staleCommentDeletionCannotUndoApproval() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            Comment stale = em.find(Comment.class, commentId);
            independent.executeWithoutResult(other -> reports.approve(commentReportId, actorId));
            stale.softDelete();
        })).isInstanceOf(OptimisticLockingFailureException.class);
        tx.executeWithoutResult(status -> {
            Comment comment = em.find(Comment.class, commentId);
            assertThat(comment.isHidden()).isTrue();
            assertThat(comment.isDeleted()).isFalse();
        });
    }

    @Test
    void staleApprovalCannotUndoCommentDeletion() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            em.find(Comment.class, commentId);
            independent.executeWithoutResult(other -> em.find(Comment.class, commentId).softDelete());
            reports.approve(commentReportId, actorId);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        tx.executeWithoutResult(status -> {
            assertThat(em.find(Comment.class, commentId).isDeleted()).isTrue();
            assertThat(em.find(Report.class, commentReportId).getStatus()).isEqualTo(ReportStatus.PENDING);
        });
    }

    @Test
    void ownershipPredicateMatchesItsName() {
        tx.executeWithoutResult(status -> {
            Post post = em.find(Post.class, postId);
            assertThat(post.isOwnedBy(ownerId)).isTrue();
            assertThat(post.isOwnedBy(actorId)).isFalse();
            assertThat(post.isOwnedBy(null)).isFalse();
            assertThat(new Post().isOwnedBy(ownerId)).isFalse();
        });
    }

    @Test
    void ownerCanModifyAndOthersCannotModifyOrDelete() {
        assertThat(posts.updatePost(postId, ownerId, new PostUpdateRequest("updated", "content")).title())
                .isEqualTo("updated");
        assertThatThrownBy(() -> posts.updatePost(postId, actorId, new PostUpdateRequest("denied", "content")))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.POST_ACCESS_DENIED));
        assertThatThrownBy(() -> posts.deletePost(postId, actorId)).isInstanceOf(BusinessException.class);
        posts.deletePost(postId, ownerId);
        tx.executeWithoutResult(status -> assertThat(em.find(Post.class, postId).isDeleted()).isTrue());
    }

    @Test
    void privatePostReadStillAllowsOnlyOwner() {
        tx.executeWithoutResult(status -> ReflectionTestUtils.setField(em.find(Post.class, postId), "isPrivate", true));
        assertThat(posts.findPost(postId, ownerId).title()).isEqualTo("original");
        for (Long userId : new Long[]{actorId, null}) {
            assertThatThrownBy(() -> posts.findPost(postId, userId))
                    .isInstanceOfSatisfying(BusinessException.class,
                            error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.POST_ACCESS_DENIED));
        }
    }
}
