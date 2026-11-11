package com.leets.choyeji;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.auth.JwtMemberResolver;
import com.leets.choyeji.auth.JwtTokenService;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:login-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=integration-test-secret-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginApiIntegrationTest {
    private static final String SECRET = "integration-test-secret-32-bytes-long";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;
    @Autowired JwtMemberResolver resolver;

    @BeforeEach
    void setUp() {
        comments.deleteAll();
        posts.deleteAll();
        members.deleteAll();
    }

    @Test
    void signupThenLoginReturnsVerifiableOneHourToken() throws Exception {
        signup("password123");
        var response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(" Yeji@Example.COM ", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String accessToken = mapper.readTree(response).path("data").path("accessToken").asText();
        Long memberId = members.findFirstByEmailOrderByIdAsc("yeji@example.com").orElseThrow().getId();
        assertThat(resolver.resolve("Bearer " + accessToken)).isEqualTo(memberId);
        var jwt = SignedJWT.parse(accessToken);
        var claims = jwt.getJWTClaimsSet();
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
        assertThat(claims.getSubject()).isEqualTo(memberId.toString());
        assertThat(claims.getExpirationTime().toInstant().getEpochSecond()
                - claims.getIssueTime().toInstant().getEpochSecond()).isEqualTo(3600);
        assertThat(claims.getClaims()).doesNotContainKeys("password", "email");
        assertThat(members.count()).isEqualTo(1);
    }

    @Test
    void unknownEmailAndWrongPasswordReturnSame401() throws Exception {
        signup("password123");
        for (String[] input : new String[][]{
                {"unknown@example.com", "password123"},
                {"yeji@example.com", "wrong-password"},
                {"yeji@example.com", "가".repeat(25)}
        }) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(body(input[0], input[1])))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("AUTH_401_2"))
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Test
    void invalidAndMissingInputReturn400() throws Exception {
        for (String input : new String[]{"{}", "{", body("bad-email", "password123"),
                body("yeji@example.com", ""), body("yeji@example.com", "a".repeat(51))}) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(input))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_400_1"));
        }
    }

    @Test
    void passwordsAreNotTrimmedAndMultibyteBoundaryWorks() throws Exception {
        String password = "가".repeat(23) + "  ";
        signup(password);
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body("yeji@example.com", password)))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body("yeji@example.com", password.strip())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidExpiredAndWrongSignatureTokensAreRejected() throws Exception {
        for (String authorization : new String[]{"Bearer invalid", "Basic token",
                signedToken(SECRET, Instant.now().minusSeconds(120)),
                signedToken("wrong-signature-secret-32-bytes-long", Instant.now().plusSeconds(3600)),
                signedToken(SECRET, null)}) {
            assertThatThrownBy(() -> resolver.resolve(authorization))
                    .isInstanceOfSatisfying(ApiException.class,
                            exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
        }
        assertThatThrownBy(() -> resolver.resolve(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void customMemberIdClaimCanBeIssuedAndVerified() {
        var issuer = new JwtTokenService(SECRET, "memberId");
        var customResolver = new JwtMemberResolver(SECRET, "memberId");
        assertThat(customResolver.resolve("Bearer " + issuer.issue(42L))).isEqualTo(42L);
        assertThatThrownBy(() -> new JwtTokenService("too-short", "sub"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void signup(String password) throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "yeji@example.com",
                                "password", password, "nickname", "예지"))))
                .andExpect(status().isOk());
    }

    private String body(String email, String password) throws Exception {
        return mapper.writeValueAsString(Map.of("email", email, "password", password));
    }

    private String signedToken(String secret, Instant expiry) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject("1");
        if (expiry != null) {
            claims.expirationTime(Date.from(expiry));
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(secret));
        return "Bearer " + jwt.serialize();
    }
}
