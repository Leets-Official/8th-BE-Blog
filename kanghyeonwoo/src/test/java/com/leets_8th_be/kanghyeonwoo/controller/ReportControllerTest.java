package com.leets_8th_be.kanghyeonwoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.entity.*;
import com.leets_8th_be.kanghyeonwoo.repository.*;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;
    @Autowired ReportRepository reports;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    User author;
    User reporter;
    User processor;
    Post post;

    @BeforeEach
    void setUp() {
        reports.deleteAll();
        posts.deleteAll();
        users.deleteAll();
        author = users.save(User.create("작성자", "moderation-author@example.com"));
        reporter = users.save(User.create("신고자", "moderation-reporter@example.com"));
        processor = users.save(User.create("처리자", "moderation-processor@example.com"));
        post = posts.save(Post.create("신고 당시 제목", "신고 당시 본문", author));
    }

    @Test
    void createsPendingReportAndRejectsADuplicate() throws Exception {
        reportPost(reporter.getId()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("S201"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.targetType").value("POST"))
                .andExpect(jsonPath("$.data.targetId").value(post.getId()));
        reportPost(reporter.getId()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPORT4091"));
        assertThat(reports.findByTargetTypeAndTargetId(ReportTargetType.POST, post.getId()))
                .singleElement().satisfies(report -> {
                    assertThat(report.getTargetTitleSnapshot()).isEqualTo("신고 당시 제목");
                    assertThat(report.getTargetContentSnapshot()).isEqualTo("신고 당시 본문");
                });
    }

    @Test
    void reportsCommentsAndRejectsSelfDeletedAndUnknownTargets() throws Exception {
        Comment comment = comments.save(Comment.create("댓글 원문", author, post));
        reportComment(comment.getId(), reporter.getId()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetType").value("COMMENT"));
        reportComment(comment.getId(), reporter.getId()).andExpect(status().isConflict());
        reportComment(comment.getId(), processor.getId()).andExpect(status().isCreated());
        reportComment(comment.getId(), author.getId()).andExpect(status().isForbidden());
        reportComment(999999L, reporter.getId()).andExpect(status().isNotFound());
        reportPost(author.getId()).andExpect(status().isForbidden());
        reportPost(999999L).andExpect(status().isNotFound());
        new TransactionTemplate(transactions).executeWithoutResult(tx -> comments.findById(comment.getId()).orElseThrow().delete());
        reportComment(comment.getId(), reporter.getId()).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/posts/{id}/reports", post.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("reporterId", reporter.getId(), "reason", " "))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/posts/-1/reports").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("reporterId", reporter.getId(), "reason", "사유"))))
                .andExpect(status().isBadRequest());
    }

    ResultActions reportComment(long id, long reporterId) throws Exception {
        return mvc.perform(post("/api/v1/comments/{id}/reports", id).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("reporterId", reporterId, "reason", "욕설 댓글"))));
    }

    @Test
    void searchesPreservedSnapshotsWithCombinedFiltersAndPagination() throws Exception {
        reportPost(reporter.getId()).andExpect(status().isCreated());
        reportPost(processor.getId()).andExpect(status().isCreated());
        new TransactionTemplate(transactions).executeWithoutResult(tx ->
                posts.findForUpdate(post.getId()).orElseThrow().update("수정 제목", "현재 원문"));
        mvc.perform(get("/api/v1/reports").param("status", "PENDING").param("targetType", "POST")
                        .param("targetId", post.getId().toString()).param("keyword", "신고 당시 본문").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.items[0].targetTitleSnapshot").value("신고 당시 제목"));
        mvc.perform(get("/api/v1/reports").param("keyword", "현재 원문"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/v1/reports").param("keyword", "욕설"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/v1/reports").param("status", "UNKNOWN")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/reports").param("targetId", "-1")).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/posts/{id}", post.getId()).param("authorId", author.getId().toString()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/reports").param("keyword", "신고 당시 제목"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void hidesCurrentPostAndKeepNeverRestoresIt() throws Exception {
        long first = reportId(reportPost(reporter.getId()));
        long second = reportId(reportPost(processor.getId()));
        mvc.perform(patch("/api/v1/posts/{id}", post.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("authorId", author.getId(), "title", "수정된 제목", "content", "수정 본문"))))
                .andExpect(status().isOk());
        resolve(first, "HIDE").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.action").value("HIDE"))
                .andExpect(jsonPath("$.data.processorId").value(processor.getId()))
                .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.targetHidden").value(true));
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/posts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/v1/posts").param("keyword", "수정"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/v1/posts/{id}/comments", post.getId())).andExpect(status().isNotFound());
        resolve(second, "KEEP").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetHidden").value(true));
        resolve(first, "KEEP").andExpect(status().isConflict());
        mvc.perform(patch("/api/v1/posts/{id}", post.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("authorId", author.getId(), "title", "다시 수정", "content", "새 본문"))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isNotFound());
        reportPost(author.getId()).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/reports").param("status", "RESOLVED").param("keyword", "신고 당시"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void hidesOnlyTheReportedCommentAndKeepsReplyRelations() throws Exception {
        Comment parent = comments.save(Comment.create("신고할 질문", author, post));
        Comment reply = comments.save(Comment.builder().content("정상 답변").user(reporter).post(post).parent(parent).build());
        long report = reportId(reportComment(parent.getId(), reporter.getId()));
        long other = reportId(reportComment(parent.getId(), processor.getId()));
        resolve(report, "HIDE").andExpect(status().isOk());
        resolve(other, "HIDE").andExpect(status().isOk());
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/posts/{id}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(reply.getId()))
                .andExpect(jsonPath("$.data.items[0].parentId").value(parent.getId()))
                .andExpect(jsonPath("$.data.items[1].content").value("숨김 처리된 댓글입니다."));
        mvc.perform(get("/api/v1/posts/{id}/comments", post.getId()).param("keyword", "질문"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        reportComment(parent.getId(), reporter.getId()).andExpect(status().isNotFound());
    }

    @Test
    void keepsActiveContentAndNeverAllowsTheSameReporterToReportAgain() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        resolve(report, "KEEP").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAvailable").value(true))
                .andExpect(jsonPath("$.data.targetHidden").value(false));
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isOk());
        reportPost(reporter.getId()).andExpect(status().isConflict());
        resolve(report, "HIDE").andExpect(status().isConflict());
    }

    @Test
    void deletedTargetsCanBeKeptButCannotBeHidden() throws Exception {
        Comment comment = comments.save(Comment.create("삭제될 댓글", author, post));
        long commentReport = reportId(reportComment(comment.getId(), reporter.getId()));
        long postReport = reportId(reportPost(reporter.getId()));
        new TransactionTemplate(transactions).executeWithoutResult(tx -> comments.findForUpdate(comment.getId()).orElseThrow().delete());
        resolve(commentReport, "HIDE").andExpect(status().isConflict());
        resolve(commentReport, "KEEP").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAvailable").value(false))
                .andExpect(jsonPath("$.data.targetHidden").isEmpty());
        mvc.perform(delete("/api/v1/posts/{id}", post.getId()).param("authorId", author.getId().toString()))
                .andExpect(status().isOk());
        reportComment(comment.getId(), reporter.getId()).andExpect(status().isNotFound());
        resolve(postReport, "HIDE").andExpect(status().isConflict());
        resolve(postReport, "KEEP").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAvailable").value(false));
        mvc.perform(get("/api/v1/reports").param("keyword", "삭제될 댓글"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void concurrentPostReportsCreateExactlyOneReport() throws Exception {
        var statuses = concurrently(8, () -> reportPost(reporter.getId()).andReturn().getResponse().getStatus());
        assertThat(statuses).filteredOn(code -> code == 201).hasSize(1);
        assertThat(statuses).filteredOn(code -> code == 409).hasSize(7);
        mvc.perform(get("/api/v1/reports").param("targetType", "POST").param("targetId", post.getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void concurrentCommentReportsCreateExactlyOneReport() throws Exception {
        Comment comment = comments.save(Comment.create("동시 신고 댓글", author, post));
        var statuses = concurrently(8, () -> reportComment(comment.getId(), reporter.getId()).andReturn().getResponse().getStatus());
        assertThat(statuses).filteredOn(code -> code == 201).hasSize(1);
        assertThat(statuses).filteredOn(code -> code == 409).hasSize(7);
    }

    @Test
    void concurrentResolutionsPreserveTheFirstDecision() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        var statuses = concurrently(8, () -> resolve(report, "HIDE").andReturn().getResponse().getStatus());
        assertThat(statuses).filteredOn(code -> code == 200).hasSize(1);
        assertThat(statuses).filteredOn(code -> code == 409).hasSize(7);
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isNotFound());
    }

    @Test
    void failedHidingRollsBackTheReportAndAllowsRetry() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        jdbc.execute("ALTER TABLE posts ADD CONSTRAINT test_hide_failure CHECK (status <> 'HIDDEN')");
        try {
            resolve(report, "HIDE").andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports").param("status", "PENDING"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
            mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isOk());
        } finally {
            jdbc.execute("ALTER TABLE posts DROP CONSTRAINT test_hide_failure");
        }
        resolve(report, "HIDE").andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidProcessingAndFilterInputs() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        for (var body : java.util.List.of(
                Map.of("processorId", processor.getId(), "action", "RESTORE", "resolutionReason", "사유"),
                Map.of("processorId", -1, "action", "HIDE", "resolutionReason", "사유"),
                Map.of("processorId", processor.getId(), "action", "KEEP", "resolutionReason", " "),
                Map.of("processorId", processor.getId(), "action", "KEEP", "resolutionReason", "x".repeat(256)))) {
            mvc.perform(patch("/api/v1/reports/{id}/resolve", report).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        }
        mvc.perform(patch("/api/v1/reports/{id}/resolve", report).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("processorId", 999999, "action", "KEEP", "resolutionReason", "사유"))))
                .andExpect(status().isNotFound());
        resolve(999999L, "KEEP").andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/reports").param("targetType", "UNKNOWN")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/reports").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/posts/{id}/comments", post.getId()).param("size", "0")).andExpect(status().isBadRequest());
    }

    private java.util.List<Integer> concurrently(int count, java.util.concurrent.Callable<Integer> request) throws Exception {
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < count; i++) futures.add(executor.submit(() -> { start.await(); return request.call(); }));
            start.countDown();
            var statuses = new java.util.ArrayList<Integer>();
            for (var future : futures) statuses.add(future.get(15, java.util.concurrent.TimeUnit.SECONDS));
            return statuses;
        }
    }

    @Test
    void concurrentDifferentDecisionsPreserveTheSuccessfulAction() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        var next = new java.util.concurrent.atomic.AtomicInteger();
        var results = new java.util.concurrent.ConcurrentHashMap<String, Integer>();
        concurrently(2, () -> {
            String action = next.getAndIncrement() == 0 ? "KEEP" : "HIDE";
            int code = resolve(report, action).andReturn().getResponse().getStatus();
            results.put(action, code);
            return code;
        });
        assertThat(results.values()).containsExactlyInAnyOrder(200, 409);
        mvc.perform(get("/api/v1/reports").param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].action").value(results.get("HIDE") == 200 ? "HIDE" : "KEEP"));
        mvc.perform(get("/api/v1/posts/{id}", post.getId()))
                .andExpect(results.get("HIDE") == 200 ? status().isNotFound() : status().isOk());
    }

    @Test
    void hidingAndEditingCannotRestoreAnActiveState() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        var next = new java.util.concurrent.atomic.AtomicInteger();
        var codes = concurrently(2, () -> next.getAndIncrement() == 0
                ? resolve(report, "HIDE").andReturn().getResponse().getStatus()
                : mvc.perform(patch("/api/v1/posts/{id}", post.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("authorId", author.getId(), "title", "동시 수정", "content", "수정 본문"))))
                        .andReturn().getResponse().getStatus());
        assertThat(codes).containsOnly(200);
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isNotFound());
    }

    @Test
    void hidingAndDeletingPreserveTheReportWithoutPartialResolution() throws Exception {
        long report = reportId(reportPost(reporter.getId()));
        var next = new java.util.concurrent.atomic.AtomicInteger();
        var codes = concurrently(2, () -> next.getAndIncrement() == 0
                ? resolve(report, "HIDE").andReturn().getResponse().getStatus()
                : mvc.perform(delete("/api/v1/posts/{id}", post.getId()).param("authorId", author.getId().toString()))
                        .andReturn().getResponse().getStatus());
        assertThat(codes).allMatch(code -> code == 200 || code == 409);
        mvc.perform(get("/api/v1/posts/{id}", post.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/reports").param("keyword", "신고 당시 본문"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        var retry = resolve(report, "KEEP").andReturn().getResponse().getStatus();
        assertThat(retry).isIn(200, 409);
        mvc.perform(get("/api/v1/reports").param("status", "PENDING"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void deletingAPostWithRepliesPreservesTheirReportSnapshots() throws Exception {
        Comment parent = comments.save(Comment.create("부모 신고 원문", author, post));
        Comment reply = comments.save(Comment.builder().content("대댓글 신고 원문").user(author).post(post).parent(parent).build());
        reportId(reportComment(parent.getId(), reporter.getId()));
        long replyReport = reportId(reportComment(reply.getId(), reporter.getId()));
        mvc.perform(delete("/api/v1/posts/{id}", post.getId()).param("authorId", author.getId().toString()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/reports").param("keyword", "신고 원문"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(2));
        resolve(replyReport, "KEEP").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAvailable").value(false));
    }

    long reportId(ResultActions result) throws Exception {
        return json.readTree(result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .path("data").path("reportId").asLong();
    }

    ResultActions resolve(long reportId, String action) throws Exception {
        return mvc.perform(patch("/api/v1/reports/{id}/resolve", reportId).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("processorId", processor.getId(), "action", action,
                        "resolutionReason", "검토 완료"))));
    }

    @AfterEach
    void cleanUp() {
        reports.deleteAll();
        posts.deleteAll();
        users.deleteAll();
    }

    ResultActions reportPost(long reporterId) throws Exception {
        return mvc.perform(post("/api/v1/posts/{id}/reports", post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("reporterId", reporterId, "reason", "욕설 신고"))));
    }
}
