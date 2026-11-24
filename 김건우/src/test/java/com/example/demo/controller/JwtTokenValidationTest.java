package com.example.demo.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 토큰 검증 항목별 거절 테스트: 만료, 잘못된 수신자(aud), 잘못된 발급자(iss), 다른 키로 서명(위조)
// 정상 토큰은 인증을 통과해야 하므로(존재하지 않는 회원이라 404), 거절 테스트가 우연히 통과한 것이 아님을 보장한다.
@SpringBootTest(properties = "jwt.secret=" + JwtTokenValidationTest.SECRET)
@AutoConfigureMockMvc
@Transactional
class JwtTokenValidationTest {

    static final String SECRET = "test-secret-key-for-leets-week6-jwt-0123456789";
    static final String OTHER_SECRET = "another-secret-key-for-leets-week6-jwt-9876543210";

    @Autowired
    MockMvc mvc;

    @Test
    void 올바르게_서명된_토큰은_인증을_통과한다() throws Exception {
        // 인증은 통과하고, 토큰의 회원(999999)이 DB에 없어서 404 가 나온다. (401 이 아님이 핵심)
        me(token(SECRET, "leets-auth", "leets-api", Instant.now().plusSeconds(600)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_404_1"));
    }

    @Test
    void 만료된_토큰은_401() throws Exception {
        me(token(SECRET, "leets-auth", "leets-api", Instant.now().minusSeconds(60)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_2"));
    }

    @Test
    void 수신자가_다른_토큰은_401() throws Exception {
        me(token(SECRET, "leets-auth", "other-api", Instant.now().plusSeconds(600)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 발급자가_다른_토큰은_401() throws Exception {
        me(token(SECRET, "other-issuer", "leets-api", Instant.now().plusSeconds(600)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 다른_키로_서명한_위조_토큰은_401() throws Exception {
        me(token(OTHER_SECRET, "leets-auth", "leets-api", Instant.now().plusSeconds(600)))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions me(String token) throws Exception {
        return mvc.perform(get("/users/me").header("Authorization", "Bearer " + token));
    }

    // JwtProvider 와 같은 형식의 access 토큰을 직접 만든다. (iss, aud, 서명 키, 만료 시각만 바꿔가며 사용)
    private String token(String secret, String issuer, String audience, Instant expiresAt) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("999999")
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("type", "access")
                .issuedAt(Date.from(expiresAt.minusSeconds(600)))
                .expiration(Date.from(expiresAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
