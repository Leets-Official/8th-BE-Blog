package com.leets.choyeji;

import com.leets.choyeji.auth.JwtTokenService;
import com.leets.choyeji.entity.Comment;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.MemberRole;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.entity.PostStatus;
import com.leets.choyeji.entity.Report;
import com.leets.choyeji.entity.ReportResult;
import com.leets.choyeji.entity.ReportStatus;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import com.leets.choyeji.repository.ReportRepository;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=integration-test-secret-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ReportRepository reports;
    @Autowired CommentRepository comments;
    @Autowired PostRepository posts;
    @Autowired MemberRepository members;
    @Autowired JwtTokenService tokens;
    @Autowired JdbcTemplate jdbc;
    private Member admin;
    private Member owner;
    private Member reporter;
    private Post post;
    private Report report;
    private String adminToken;
    private String ownerToken;
    private String reporterToken;

    @BeforeEach
    void setUp() {
        reports.deleteAll();
        comments.deleteAll();
        posts.deleteAll();
        members.deleteAll();
        admin = members.save(new Member("unused", "관리자", "admin@example.com"));
        owner = members.save(new Member("unused", "작성자", "owner@example.com"));
        reporter = members.save(new Member("unused", "신고자", "reporter@example.com"));
        jdbc.update("update member set role = 'ADMIN' where member_id = ?", admin.getId());
        post = posts.save(new Post(owner, "게시글", "내용"));
        report = reports.save(new Report(post, reporter, "신고 사유"));
        adminToken = "Bearer " + tokens.issue(admin.getId());
        ownerToken = "Bearer " + tokens.issue(owner.getId());
        reporterToken = "Bearer " + tokens.issue(reporter.getId());
    }

    @Test
    void acceptResolvesReportHidesPostAndBlocksPublicAccess() throws Exception {
        comments.save(new Comment(post, reporter, "기존 댓글"));
        Post visible = posts.save(new Post(owner, "공개 글", "내용"));
        mvc.perform(patch("/admin/reports/{id}/resolve", report.getId()).header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"result\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.result").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.postStatus").value("HIDDEN"))
                .andExpect(jsonPath("$.data.updatedAt").isString());
        assertThat(jdbc.queryForObject("select status from post where post_id = ?", String.class, post.getId()))
                .isEqualTo("HIDDEN");
        assertThat(jdbc.queryForObject("select status from report where id = ?", String.class, report.getId()))
                .isEqualTo("RESOLVED");
        mvc.perform(get("/posts/{id}", post.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/posts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.posts[0].postId").value(visible.getId()))
                .andExpect(jsonPath("$.data.posts[0].status").value("ACTIVE"));
        mvc.perform(get("/posts/{id}/comments", post.getId())).andExpect(status().isNotFound());
        mvc.perform(post("/posts/{id}/comments", post.getId()).header("Authorization", reporterToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"댓글\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/posts/{id}/reports", post.getId()).header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"추가 신고\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/posts/{id}", post.getId()).header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"수정\",\"content\":\"내용\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/posts/{id}", post.getId()).header("Authorization", ownerToken))
                .andExpect(status().isNotFound());
        assertThat(comments.count()).isEqualTo(1);
        assertThat(reports.count()).isEqualTo(1);
    }

    @Test
    void rejectKeepsPostActiveAndReprocessingReturns409() throws Exception {
        resolve(report.getId(), "REJECTED", 200);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.ACTIVE);
        resolve(report.getId(), "ACCEPTED", 409);
        resolve(report.getId(), "REJECTED", 409);
        Report saved = reports.findById(report.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(saved.getResult()).isEqualTo(ReportResult.REJECTED);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.ACTIVE);
    }

    @Test
    void rejectingAnotherReportDoesNotUnhideAnAcceptedPost() throws Exception {
        Report other = reports.save(new Report(post, owner, "다른 신고"));
        resolve(report.getId(), "ACCEPTED", 200);
        resolve(other.getId(), "REJECTED", 200);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void restoreIsIdempotentPreservesReportsAndRevealsExistingComments() throws Exception {
        comments.save(new Comment(post, reporter, "기존 댓글"));
        resolve(report.getId(), "ACCEPTED", 200);
        for (int i = 0; i < 2; i++) {
            mvc.perform(patch("/admin/posts/{id}/restore", post.getId()).header("Authorization", adminToken))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }
        mvc.perform(get("/posts/{id}", post.getId())).andExpect(status().isOk());
        mvc.perform(get("/posts/{id}/comments", post.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("기존 댓글"));
        assertThat(reports.findById(report.getId()).orElseThrow().getResult()).isEqualTo(ReportResult.ACCEPTED);
        resolve(report.getId(), "ACCEPTED", 409);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.ACTIVE);
    }

    @Test
    void adminListFiltersAndPaginatesWithCurrentPostState() throws Exception {
        Report newer = reports.save(new Report(post, owner, "두 번째 신고"));
        resolve(report.getId(), "ACCEPTED", 200);
        mvc.perform(get("/admin/reports").header("Authorization", adminToken).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.reports[0].id").value(newer.getId()))
                .andExpect(jsonPath("$.data.reports[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$.data.reports[0].postStatus").value("HIDDEN"));
        mvc.perform(get("/admin/reports").header("Authorization", adminToken).param("status", "PENDING"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.reports[0].id").value(newer.getId()));
        mvc.perform(get("/admin/reports").header("Authorization", adminToken).param("status", "RESOLVED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.reports[0].id").value(report.getId()));
    }

    @Test
    void allAdminEndpointsRequireAuthenticationAndAdminRole() throws Exception {
        for (String bearer : new String[]{"", "Bearer invalid", "Bearer " + tokens.issue(999999L), ownerToken}) {
            int expected = bearer.equals(ownerToken) ? 403 : 401;
            mvc.perform(get("/admin/reports").header("Authorization", bearer))
                    .andExpect(status().is(expected));
            mvc.perform(patch("/admin/reports/{id}/resolve", report.getId()).header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"result\":\"ACCEPTED\"}"))
                    .andExpect(status().is(expected));
            mvc.perform(patch("/admin/posts/{id}/restore", post.getId()).header("Authorization", bearer))
                    .andExpect(status().is(expected));
        }
        assertThat(reports.findById(report.getId()).orElseThrow().getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.ACTIVE);
    }

    @Test
    void signupCannotGrantAdminAndRevocationAppliesToExistingToken() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"password\":\"password123\",\"nickname\":\"new\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());
        assertThat(members.findFirstByEmailOrderByIdAsc("new@example.com").orElseThrow().getRole())
                .isEqualTo(MemberRole.USER);
        mvc.perform(get("/admin/reports").header("Authorization", adminToken)).andExpect(status().isOk());
        jdbc.update("update member set role = 'USER' where member_id = ?", admin.getId());
        mvc.perform(get("/admin/reports").header("Authorization", adminToken))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403_1"));
    }

    @Test
    void invalidResultsAndMissingTargetsDoNotChangeState() throws Exception {
        for (String body : new String[]{"{}", "{\"result\":null}", "{\"result\":\"INVALID\"}"}) {
            mvc.perform(patch("/admin/reports/{id}/resolve", report.getId()).header("Authorization", adminToken)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/admin/reports").header("Authorization", adminToken).param("status", "INVALID"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/admin/reports/999999/resolve").header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"result\":\"ACCEPTED\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("REPORT_404_1"));
        mvc.perform(patch("/admin/posts/999999/restore").header("Authorization", adminToken))
                .andExpect(status().isNotFound());
        assertThat(reports.findById(report.getId()).orElseThrow().getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.ACTIVE);
    }

    @Test
    void concurrentConflictingResolutionsAllowExactlyOneWinner() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Callable<Integer> accepted = resolutionTask(start, report.getId(), "ACCEPTED");
            Callable<Integer> rejected = resolutionTask(start, report.getId(), "REJECTED");
            var first = executor.submit(accepted);
            var second = executor.submit(rejected);
            start.countDown();
            assertThat(new Integer[]{first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(200, 409);
            Report saved = reports.findById(report.getId()).orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ReportStatus.RESOLVED);
            PostStatus expected = saved.getResult() == ReportResult.ACCEPTED ? PostStatus.HIDDEN : PostStatus.ACTIVE;
            assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(expected);
        }
    }

    private Callable<Integer> resolutionTask(CountDownLatch start, Long id, String result) {
        return () -> {
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 요청 시작 시간 초과");
            }
            return mvc.perform(patch("/admin/reports/{id}/resolve", id).header("Authorization", adminToken)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"result\":\"" + result + "\"}"))
                    .andReturn().getResponse().getStatus();
        };
    }

    private void resolve(Long id, String result, int expected) throws Exception {
        var response = mvc.perform(patch("/admin/reports/{id}/resolve", id).header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"result\":\"" + result + "\"}"))
                .andExpect(status().is(expected));
        if (expected == 409) {
            response.andExpect(jsonPath("$.code").value("REPORT_409_2"));
        }
    }
}
