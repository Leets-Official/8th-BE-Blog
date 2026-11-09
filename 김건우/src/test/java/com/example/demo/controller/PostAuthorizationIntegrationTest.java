package com.example.demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 인증/인가 통합 테스트: 사용자는 요청 값이 아니라 검증된 토큰에서 결정된다.
// Given(작성자 A, 타인 B) - When(요청) - Then(상태 코드 + 실제 데이터 변화)
@SpringBootTest(properties = "jwt.secret=test-secret-key-for-leets-week6-jwt-0123456789")
@AutoConfigureMockMvc
@Transactional
class PostAuthorizationIntegrationTest {

    @Autowired
    MockMvc mvc;

    String tokenA; // 글 작성자
    String tokenB; // 타인

    @BeforeEach
    void setUp() throws Exception {
        tokenA = signupAndLogin("a@test.com", "writerA");
        tokenB = signupAndLogin("b@test.com", "otherB");
    }

    @Test
    void 인증_없이_게시글을_작성하면_401() throws Exception {
        json(post("/posts"), null, "{\"title\":\"제목\",\"content\":\"내용\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_2"));
    }

    @Test
    void 작성자는_요청_본문의_userId가_아니라_토큰의_사용자로_결정된다() throws Exception {
        // 본문에 다른 userId 를 실어 보내도 무시된다. (클라이언트가 바꿀 수 있는 값은 신뢰하지 않음)
        json(post("/posts"), tokenA, "{\"userId\":999,\"title\":\"제목\",\"content\":\"내용\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.writer").value("writerA"));
    }

    @Test
    void 타인의_글을_수정하면_403이고_원본이_유지된다() throws Exception {
        long postId = createPost(tokenA, "원래 제목");

        json(put("/posts/{id}", postId), tokenB, "{\"title\":\"바뀐 제목\",\"content\":\"바뀐 내용\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON_403_1"));

        mvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("원래 제목"));
    }

    @Test
    void 작성자가_자신의_글을_수정하면_성공한다() throws Exception {
        long postId = createPost(tokenA, "원래 제목");

        json(patch("/posts/{id}", postId), tokenA, "{\"title\":\"바뀐 제목\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("바뀐 제목"));
    }

    @Test
    void 타인의_글을_삭제하면_403이고_글은_남아있다() throws Exception {
        long postId = createPost(tokenA, "원래 제목");

        mvc.perform(delete("/posts/{id}", postId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        mvc.perform(get("/posts/{id}", postId)).andExpect(status().isOk());
    }

    @Test
    void 인증_없이_삭제하면_401이고_글은_남아있다() throws Exception {
        long postId = createPost(tokenA, "원래 제목");

        mvc.perform(delete("/posts/{id}", postId)).andExpect(status().isUnauthorized());

        mvc.perform(get("/posts/{id}", postId)).andExpect(status().isOk());
    }

    @Test
    void 본인_글_신고는_400이고_타인_글_신고는_201() throws Exception {
        long postId = createPost(tokenA, "신고 대상");
        String body = "{\"reason\":\"광고성 게시글입니다.\"}";

        json(post("/posts/{id}/reports", postId), tokenA, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_400_1"));

        json(post("/posts/{id}/reports", postId), tokenB, body)
                .andExpect(status().isCreated());
    }

    @Test
    void 타인이_댓글을_채택하면_403() throws Exception {
        long postId = createPost(tokenA, "채택 테스트");
        String commentBody = json(post("/posts/{id}/comments", postId), tokenB, "{\"content\":\"댓글\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number commentId = JsonPath.read(commentBody, "$.data.commentId");

        // 게시글 작성자(A)가 아닌 B 가 채택 시도
        mvc.perform(post("/posts/{postId}/comments/{commentId}/accept", postId, commentId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMENT_403_1"));
    }

    // ---------- helpers ----------

    private ResultActions json(MockHttpServletRequestBuilder builder, String token, String body) throws Exception {
        builder.contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(builder);
    }

    private long createPost(String token, String title) throws Exception {
        String body = json(post("/posts"), token, "{\"title\":\"" + title + "\",\"content\":\"내용\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.postId")).longValue();
    }

    private String signupAndLogin(String email, String nickname) throws Exception {
        json(post("/auth/signup"), null,
                "{\"email\":\"%s\",\"password\":\"password1234!\",\"nickname\":\"%s\"}".formatted(email, nickname))
                .andExpect(status().isCreated());
        String body = json(post("/auth/login"), null,
                "{\"email\":\"%s\",\"password\":\"password1234!\"}".formatted(email))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.accessToken");
    }
}
