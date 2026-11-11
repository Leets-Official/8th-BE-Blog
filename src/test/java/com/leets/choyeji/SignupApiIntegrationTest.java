package com.leets.choyeji;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:signup-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=integration-test-secret-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignupApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        comments.deleteAll();
        posts.deleteAll();
        members.deleteAll();
    }

    @Test
    void signupNormalizesEmailAndStoresHashedPassword() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(body("  Yeji@Example.COM  ", "password123", "예지")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.memberId").isNumber())
                .andExpect(jsonPath("$.data.email").value("yeji@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("예지"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        var member = members.findFirstByEmailOrderByIdAsc("yeji@example.com").orElseThrow();
        assertThat(member.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", member.getPassword())).isTrue();
    }

    @Test
    void duplicateEmailReturns409AndNicknameCanBeReused() throws Exception {
        signup("yeji@example.com", "예지", 200);
        signup(" YEJI@EXAMPLE.COM ", "다른닉네임", 409);
        signup("other@example.com", "예지", 200);
        assertThat(members.count()).isEqualTo(2);
    }

    @Test
    void invalidInputReturns400WithoutCreatingMember() throws Exception {
        String[][] invalid = {
                {"bad-email", "password123", "예지"},
                {" ", "password123", "예지"},
                {"a@example.com", "short", "예지"},
                {"a@example.com", "x".repeat(51), "예지"},
                {"a@example.com", " ".repeat(8), "예지"},
                {"a@example.com", "password123", " "},
                {"a@example.com", "password123", "가".repeat(51)}
        };
        for (String[] input : invalid) {
            mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                            .content(body(input[0], input[1], input[2])))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_400_1"));
        }
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        assertThat(members.count()).isZero();
    }

    @Test
    void bcryptByteLimitIsEnforced() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(body("a@example.com", "가".repeat(25), "예지")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_1"));
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(body("a@example.com", "가".repeat(24), "예지")))
                .andExpect(status().isOk());
        var member = members.findFirstByEmailOrderByIdAsc("a@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("가".repeat(24), member.getPassword())).isTrue();
    }

    @Test
    void concurrentSignupCreatesOnlyOneMember() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            var task = (java.util.concurrent.Callable<Integer>) () -> {
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("동시 요청 시작 시간 초과");
                }
                return mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                                .content(body("race@example.com", "password123", "예지")))
                        .andReturn().getResponse().getStatus();
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertThat(new Integer[]{first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(200, 409);
            assertThat(members.count()).isEqualTo(1);
        }
    }

    private void signup(String email, String nickname, int expectedStatus) throws Exception {
        var result = mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(body(email, "password123", nickname)))
                .andExpect(status().is(expectedStatus));
        if (expectedStatus == 409) {
            result.andExpect(jsonPath("$.code").value("AUTH_409_1"));
        }
    }

    private String body(String email, String password, String nickname) throws Exception {
        return mapper.writeValueAsString(Map.of("email", email, "password", password, "nickname", nickname));
    }
}
