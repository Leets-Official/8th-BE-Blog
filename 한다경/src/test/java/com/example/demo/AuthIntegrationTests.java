package com.example.demo;

import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTests {
    private static final String PASSWORD = "test-password";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired PostRepository posts;
    @Autowired JwtEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    @Test
    void validatesSignupAndKeepsPasswordHashed() throws Exception {
        String nickname = UUID.randomUUID().toString();
        String email = nickname + "@example.test";
        signup(email, nickname).andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertThat(members.findByEmailAndDeletedAtIsNull(email).orElseThrow().getPasswordHash())
                .startsWith("{pbkdf2-v1}").doesNotContain(PASSWORD);

        signup(email.toUpperCase(java.util.Locale.ROOT), nickname + "-2").andExpect(status().isConflict());
        signup("other-" + email, nickname).andExpect(status().isConflict());
        login(email, "wrong-password").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        login("missing-" + email, PASSWORD).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void concurrentSignupAllowsOnlyOneNickname() throws Exception {
        String nickname = UUID.randomUUID().toString();
        assertThat(race(
                () -> signup("a-" + nickname + "@example.test", nickname).andReturn().getResponse().getStatus(),
                () -> signup("b-" + nickname + "@example.test", nickname).andReturn().getResponse().getStatus()))
                .containsExactlyInAnyOrder(201, 409);
    }

    @Test
    void usesVerifiedIdentityAndKeepsOwnershipAndAdminRules() throws Exception {
        Account author = account();
        Account reader = account();

        long postId = body(mvc.perform(post("/posts")
                .header(HttpHeaders.AUTHORIZATION, bearer(author.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"original","content":"body"}
                        """)).andExpect(status().isCreated())).path("id").asLong();
        mvc.perform(get("/posts/" + postId)).andExpect(status().isOk());

        String update = """
                {"title":"updated","content":"body"}
                """;
        mvc.perform(put("/posts/" + postId).header("X-User-Id", author.id())
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/posts/" + postId).header("X-User-Id", author.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(reader.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isForbidden());
        assertThat(posts.findById(postId).orElseThrow().getTitle()).isEqualTo("original");
        mvc.perform(put("/posts/" + postId).header(HttpHeaders.AUTHORIZATION, bearer(author.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk());
        assertThat(posts.findById(postId).orElseThrow().getTitle()).isEqualTo("updated");

        long reportId = body(mvc.perform(post("/posts/" + postId + "/reports")
                .header(HttpHeaders.AUTHORIZATION, bearer(reader.accessToken()))
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"reason":"spam"}
                        """)).andExpect(status().isCreated())).path("id").asLong();
        mvc.perform(post("/reports/" + reportId + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, bearer(reader.accessToken())))
                .andExpect(status().isForbidden());
        jdbc.update("update member set administrator = true where id = ?", reader.id());
        mvc.perform(post("/reports/" + reportId + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, bearer(reader.accessToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void rejectsInvalidAccessTokensAndRefreshTokensUsedAsAccessTokens() throws Exception {
        Account account = account();
        Instant now = Instant.now();
        String[] parts = account.accessToken().split("\\.");
        String changedSignature = (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        String tampered = String.join(".", parts[0], parts[1], changedSignature);

        for (String token : List.of(tampered, account.refreshToken(),
                signedToken(account.id(), now.minusSeconds(120), now.minusSeconds(60), TokenService.AUDIENCE),
                signedToken(account.id(), now, now.plusSeconds(60), "another-api"))) {
            mvc.perform(post("/posts").header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
                            org.hamcrest.Matchers.startsWith("Bearer")))
                    .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        }
    }

    @Test
    void rotatesRefreshTokenAtomicallyAndRejectsExpiredTokens() throws Exception {
        Account account = account();
        String refresh = account.refreshToken();
        assertThat(race(() -> refresh(refresh).andReturn().getResponse().getStatus(),
                () -> refresh(refresh).andReturn().getResponse().getStatus()))
                .containsExactlyInAnyOrder(200, 401);
        refresh(refresh).andExpect(status().isUnauthorized());
        refresh(account.accessToken()).andExpect(status().isBadRequest());

        Account other = account();
        JsonNode rotated = body(refresh(other.refreshToken()).andExpect(status().isOk()));
        String replacement = rotated.path("refreshToken").asText();
        assertThat(replacement).isNotBlank().isNotEqualTo(other.refreshToken());
        assertThat(rotated.path("accessToken").asText()).isNotBlank().isNotEqualTo(other.accessToken());
        mvc.perform(post("/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(rotated.path("accessToken").asText()))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"refreshed","content":"body"}
                                """))
                .andExpect(status().isCreated());
        jdbc.update("update refresh_token set expires_at = ? where member_id = ?",
                java.sql.Timestamp.from(Instant.now().minusSeconds(60)), other.id());
        refresh(replacement).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    private ResultActions signup(String email, String nickname) throws Exception {
        return jsonPost("/auth", Map.of("email", email, "password", PASSWORD, "nickname", nickname));
    }

    private ResultActions login(String email, String password) throws Exception {
        return jsonPost("/login", Map.of("email", email, "password", password));
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return jsonPost("/auth/refresh", Map.of("refreshToken", refreshToken));
    }

    private ResultActions jsonPost(String path, Map<String, String> request) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request)));
    }

    private Account account() throws Exception {
        String nickname = UUID.randomUUID().toString();
        String email = nickname + "@example.test";
        long id = body(signup(email, nickname).andExpect(status().isCreated())).path("memberId").asLong();
        JsonNode tokens = body(login(email, PASSWORD).andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store")));
        return new Account(id, tokens.path("accessToken").asText(), tokens.path("refreshToken").asText());
    }

    private String signedToken(long id, Instant issuedAt, Instant expiresAt, String audience) {
        var claims = JwtClaimsSet.builder().subject(Long.toString(id)).issuer(TokenService.ISSUER)
                .audience(List.of(audience)).issuedAt(issuedAt).expiresAt(expiresAt).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private JsonNode body(ResultActions actions) throws Exception {
        return mapper.readTree(actions.andReturn().getResponse().getContentAsByteArray());
    }

    private List<Integer> race(Callable<Integer> first, Callable<Integer> second) throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var one = pool.submit(() -> { start.await(); return first.call(); });
            var two = pool.submit(() -> { start.await(); return second.call(); });
            start.countDown();
            return List.of(one.get(10, TimeUnit.SECONDS), two.get(10, TimeUnit.SECONDS));
        }
    }

    private record Account(long id, String accessToken, String refreshToken) {}
}
