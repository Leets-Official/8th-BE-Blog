package com.leets.choyeji;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.auth.JwtTokenService;
import com.leets.choyeji.entity.Comment;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.entity.Report;
import com.leets.choyeji.entity.ReportStatus;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import com.leets.choyeji.repository.ReportRepository;
import java.util.Map;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:report-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=integration-test-secret-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ReportRepository reports;
    @Autowired CommentRepository comments;
    @Autowired PostRepository posts;
    @Autowired MemberRepository members;
    @Autowired JwtTokenService tokens;
    private Member owner;
    private Member reporter;
    private Post post;
    private String authorization;

    @BeforeEach
    void setUp() {
        reports.deleteAll();
        comments.deleteAll();
        posts.deleteAll();
        members.deleteAll();
        owner = members.save(new Member("unused", "글작성자", "owner@example.com"));
        reporter = members.save(new Member("unused", "신고자", "reporter@example.com"));
        post = posts.save(new Post(owner, "게시글", "내용"));
        authorization = "Bearer " + tokens.issue(reporter.getId());
    }

    @Test
    void createsPendingReportUsingAuthenticatedMemberAndIdColumn() throws Exception {
        String response = mvc.perform(post("/posts/{id}/reports", post.getId())
                        .header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                        .content(body("광고성 게시글입니다.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.postId").value(post.getId()))
                .andExpect(jsonPath("$.data.reason").value("광고성 게시글입니다."))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.result").isEmpty())
                .andExpect(jsonPath("$.data.createdAt").isString())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).path("data").path("id").asLong();
        Report saved = reports.findById(id).orElseThrow();
        assertThat(saved.getMember().getId()).isEqualTo(reporter.getId());
        assertThat(saved.getPost().getId()).isEqualTo(post.getId());
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(saved.getResult()).isNull();
        assertThat(saved.getUpdatedAt()).isNull();
    }

    @Test
    void repeatedReportReturns409WithoutReplacingOriginal() throws Exception {
        submit(post.getId(), authorization, "첫 신고", 200);
        submit(post.getId(), authorization, "바뀐 사유", 409);
        assertThat(reports.count()).isEqualTo(1);
        assertThat(reports.findAll().getFirst().getReason()).isEqualTo("첫 신고");
    }

    @Test
    void duplicateRuleIsScopedToMemberAndPostPair() throws Exception {
        Member other = members.save(new Member("unused", "다른신고자", "other@example.com"));
        Post otherPost = posts.save(new Post(owner, "다른 게시글", "내용"));
        submit(post.getId(), authorization, "신고", 200);
        submit(post.getId(), "Bearer " + tokens.issue(other.getId()), "신고", 200);
        submit(otherPost.getId(), authorization, "신고", 200);
        assertThat(reports.count()).isEqualTo(3);
    }

    @Test
    void concurrentDuplicateReportsCreateOnlyOneRow() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Callable<Integer> task = () -> {
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("동시 요청 시작 시간 초과");
                }
                return mvc.perform(post("/posts/{id}/reports", post.getId())
                                .header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                                .content(body("동시 신고")))
                        .andReturn().getResponse().getStatus();
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertThat(new Integer[]{first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(200, 409);
            assertThat(reports.count()).isEqualTo(1);
        }
    }

    @Test
    void requiresAuthenticationAndExistingPost() throws Exception {
        mvc.perform(post("/posts/{id}/reports", post.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(body("신고")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
        for (String bearer : new String[]{"Bearer invalid", "Bearer " + tokens.issue(999999L)}) {
            mvc.perform(post("/posts/{id}/reports", post.getId()).header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON).content(body("신고")))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
        }
        mvc.perform(post("/posts/999999/reports").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body("신고")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POST_404_1"));
        assertThat(reports.count()).isZero();
    }

    @Test
    void validatesReasonAndAccepts500CharacterBoundary() throws Exception {
        for (String input : new String[]{"{}", "{", "{\"reason\":null}",
                body(""), body(" \n\t"), body("가".repeat(501))}) {
            mvc.perform(post("/posts/{id}/reports", post.getId()).header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(input))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON_400_1"));
        }
        assertThat(reports.count()).isZero();
        submit(post.getId(), authorization, "가".repeat(500), 200);
    }

    @Test
    void deletingPostRemovesOnlyItsCommentsAndReports() throws Exception {
        Post otherPost = posts.save(new Post(owner, "유지할 게시글", "내용"));
        comments.save(new Comment(post, reporter, "삭제할 댓글"));
        comments.save(new Comment(otherPost, reporter, "유지할 댓글"));
        submit(post.getId(), authorization, "삭제할 신고", 200);
        submit(otherPost.getId(), authorization, "유지할 신고", 200);
        mvc.perform(delete("/posts/{id}", post.getId())
                        .header("Authorization", "Bearer " + tokens.issue(owner.getId())))
                .andExpect(status().isOk());
        assertThat(posts.existsById(post.getId())).isFalse();
        assertThat(posts.existsById(otherPost.getId())).isTrue();
        assertThat(reports.count()).isEqualTo(1);
        assertThat(reports.findAll().getFirst().getPost().getId()).isEqualTo(otherPost.getId());
        assertThat(comments.count()).isEqualTo(1);
        assertThat(comments.findAll().getFirst().getPost().getId()).isEqualTo(otherPost.getId());
    }

    private void submit(Long postId, String bearer, String reason, int expectedStatus) throws Exception {
        var result = mvc.perform(post("/posts/{id}/reports", postId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(body(reason)))
                .andExpect(status().is(expectedStatus));
        if (expectedStatus == 409) {
            result.andExpect(jsonPath("$.code").value("REPORT_409_1"));
        }
    }

    private String body(String reason) throws Exception {
        return mapper.writeValueAsString(Map.of("reason", reason));
    }
}
