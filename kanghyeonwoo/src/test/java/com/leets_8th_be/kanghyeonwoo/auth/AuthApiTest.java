package com.leets_8th_be.kanghyeonwoo.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest extends AuthApiSupport {

    @Test
    void signupRejectsDuplicateEmail() throws Exception {
        String body = "{\"email\":\"auth@example.com\",\"password\":\"password123\",\"nickname\":\"auth-user\"}";
        mvc.perform(post("/api/v2/auth/signup").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v2/auth/signup").contentType("application/json").content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void rotationReplayRevokesSuccessorAndProtectedApiRequiresAccessToken() throws Exception {
        String body =
                "{\"email\":\"rotation@example.com\",\"password\":\"password123\",\"nickname\":\"rotation-user\"}";
        mvc.perform(post("/api/v2/auth/signup").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        var login = mvc.perform(post("/api/v2/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        var tokens = json.readTree(login.getResponse().getContentAsString()).get("data");
        String access = tokens.get("accessToken").asText(),
                refresh = tokens.get("refreshToken").asText();
        mvc.perform(get("/api/v2/health")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v2/health").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v2/health").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
        String req = json.writeValueAsString(Map.of("refreshToken", refresh));
        var rotated = mvc.perform(post("/api/v2/auth/reissue")
                        .contentType("application/json")
                        .content(req))
                .andExpect(status().isOk())
                .andReturn();
        String next = json.readTree(rotated.getResponse().getContentAsString())
                .get("data")
                .get("refreshToken")
                .asText();
        Assertions.assertThat(next).isNotEqualTo(refresh);
        mvc.perform(post("/api/v2/auth/reissue").contentType("application/json").content(req))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v2/auth/reissue")
                        .contentType("application/json")
                        .content(json.writeValueAsString(Map.of("refreshToken", next))))
                .andExpect(status().isUnauthorized());
    }

    @Autowired
    TokenProvider jwt;

    @Test
    void badPasswordUnknownEmailExpiredAndTamperedTokensAreRejected() throws Exception {
        var tokens = signupAndLogin("reject");
        for (String email : List.of("reject@example.com", "unknown@example.com")) {
            mvc.perform(post("/api/v2/auth/login")
                            .contentType("application/json")
                            .content(json.writeValueAsString(Map.of("email", email, "password", "wrongpass"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
        String expired = jwt.issue(1, TokenType.ACCESS, "session", Duration.ofSeconds(-10));
        String token = tokens.get("accessToken").asText();
        for (String invalid : List.of(expired, token.substring(0, token.lastIndexOf('.') + 1) + "invalid")) {
            mvc.perform(get("/api/v2/health").header("Authorization", "Bearer " + invalid))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/v2/reports").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutOnlyRevokesOneLogin() throws Exception {
        var first = signupAndLogin("logout");
        var second = mvc.perform(post("/api/v2/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"logout@example.com\",\"password\":\"password123\"}"))
                .andReturn();
        var other = json.readTree(second.getResponse().getContentAsString()).get("data");
        String body = json.writeValueAsString(
                Map.of("refreshToken", first.get("refreshToken").asText()));
        mvc.perform(post("/api/v2/auth/logout").contentType("application/json").content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v2/auth/reissue").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v2/auth/reissue")
                        .contentType("application/json")
                        .content(json.writeValueAsString(
                                Map.of("refreshToken", other.get("refreshToken").asText()))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v2/health")
                        .header(
                                "Authorization",
                                "Bearer " + first.get("accessToken").asText()))
                .andExpect(status().isOk());
    }

    @Test
    void concurrentSignupAndRefreshHaveExactlyOneWinner() throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            String body = "{\"email\":\"race@example.com\",\"password\":\"password123\",\"nickname\":\"race-user\"}";
            var ready = new CountDownLatch(2);
            var go = new CountDownLatch(1);
            Callable<Integer> signup = () -> {
                ready.countDown();
                go.await();
                return mvc.perform(post("/api/v2/auth/signup")
                                .contentType("application/json")
                                .content(body))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };
            var first = pool.submit(signup);
            var second = pool.submit(signup);
            ready.await();
            go.countDown();
            Assertions.assertThat(List.of(first.get(), second.get())).containsExactlyInAnyOrder(201, 409);
            var login = mvc.perform(post("/api/v2/auth/login")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andReturn();
            String refresh = json.readTree(login.getResponse().getContentAsString())
                    .path("data")
                    .path("refreshToken")
                    .asText();
            var readyRefresh = new CountDownLatch(2);
            var goRefresh = new CountDownLatch(1);
            Callable<Integer> rotate = () -> {
                readyRefresh.countDown();
                goRefresh.await();
                return mvc.perform(post("/api/v2/auth/reissue")
                                .contentType("application/json")
                                .content(json.writeValueAsString(Map.of("refreshToken", refresh))))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };
            var r1 = pool.submit(rotate);
            var r2 = pool.submit(rotate);
            readyRefresh.await();
            goRefresh.countDown();
            Assertions.assertThat(List.of(r1.get(), r2.get())).containsExactlyInAnyOrder(200, 401);
        }
    }
}
