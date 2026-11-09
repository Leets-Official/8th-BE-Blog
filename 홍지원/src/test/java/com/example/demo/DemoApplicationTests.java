package com.example.demo;

import com.example.demo.domain.member.Member;
import com.example.demo.domain.member.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 실제 인증 필터와 DB를 함께 검증하되 개인 MySQL 데이터는 사용하지 않습니다.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=test-only-secret-at-least-32-bytes-long"
})
@AutoConfigureMockMvc
@Transactional
class DemoApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager entityManager;

    @Test
    void signsUpWithHashedPasswordAndRejectsDuplicates() throws Exception {
        signup("member@example.com", "지원").andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nickname").value("지원"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        Member member = members.findByEmailAndDeletedAtIsNull("member@example.com").orElseThrow();
        assertTrue(passwords.matches("password123!", member.getPassword()));
        assertNotEquals("password123!", member.getPassword());
        assertEquals("USER", member.getRole());
        signup("MEMBER@example.com", "다른닉네임").andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_EMAIL"));
        signup("other@example.com", "지원").andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_NICKNAME"));
    }

    @Test
    void databaseAlsoPreventsDuplicateNicknames() {
        members.saveAndFlush(new Member("중복", null, "first@example.com", "hash"));
        assertThrows(DataIntegrityViolationException.class,
                () -> members.saveAndFlush(new Member("중복", null, "second@example.com", "hash")));
    }

    @Test
    void databaseAlsoPreventsDuplicateEmails() {
        members.saveAndFlush(new Member("첫회원", null, "same@example.com", "hash"));
        assertThrows(DataIntegrityViolationException.class,
                () -> members.saveAndFlush(new Member("다른회원", null, "same@example.com", "hash")));
    }

    @Test
    void rejectsInvalidInputAndMalformedJson() throws Exception {
        postJson("/auth", Map.of("email", "invalid", "password", "short", "nickname", " "))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        postJson("/auth", Map.of("email", "member@example.com", "password", "가".repeat(25), "nickname", "지원"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_PASSWORD"));
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        postJson("/auth/refresh", Map.of("refreshToken", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsWrongCredentialsAndDeletedMembers() throws Exception {
        signup("member@example.com", "지원");
        postJson("/login", Map.of("email", "member@example.com", "password", "wrong-password"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        postJson("/login", Map.of("email", "missing@example.com", "password", "password123!"))
                .andExpect(status().isUnauthorized());
        JsonNode tokens = login();
        members.findByEmailAndDeletedAtIsNull("member@example.com").orElseThrow().delete();
        postJson("/login", Map.of("email", "member@example.com", "password", "password123!"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/posts").header("Authorization", bearer(tokens)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
        refresh(tokens.get("refreshToken").asText()).andExpect(status().isUnauthorized());
    }

    @Test
    void issuesTokensAndRotatesRefreshToken() throws Exception {
        signup("member@example.com", "지원");
        JsonNode first = login();
        assertEquals("Bearer", first.get("tokenType").asText());
        var jwt = decoder.decode(first.get("accessToken").asText());
        assertEquals(900, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        Member member = members.findByEmailAndDeletedAtIsNull("member@example.com").orElseThrow();
        assertEquals(64, member.getRefreshTokenHash().length());
        assertNotEquals(first.get("refreshToken").asText(), member.getRefreshTokenHash());
        JsonNode second = data(refresh(first.get("refreshToken").asText()).andExpect(status().isOk()));
        assertNotEquals(first.get("accessToken"), second.get("accessToken"));
        assertNotEquals(first.get("refreshToken"), second.get("refreshToken"));
        refresh(first.get("refreshToken").asText()).andExpect(status().isUnauthorized());
        refresh(second.get("refreshToken").asText()).andExpect(status().isOk());
    }

    @Test
    void rejectsExpiredUnknownAndReplacedRefreshTokens() throws Exception {
        signup("member@example.com", "지원");
        JsonNode first = login();
        JsonNode second = login();
        refresh(first.get("refreshToken").asText()).andExpect(status().isUnauthorized());
        refresh("unknown-refresh-token").andExpect(status().isUnauthorized());
        Member member = members.findByEmailAndDeletedAtIsNull("member@example.com").orElseThrow();
        member.updateRefreshToken(member.getRefreshTokenHash(), LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));
        refresh(second.get("refreshToken").asText()).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesPostAuthorAndPreventsHeaderImpersonation() throws Exception {
        signup("member@example.com", "지원");
        JsonNode owner = login();
        long postId = data(mvc.perform(post("/posts").header("Authorization", bearer(owner))
                        .header("X-User-Id", "9999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isCreated())).get("postId").asLong();
        signup("other@example.com", "다른회원");
        JsonNode other = data(postJson("/login", Map.of("email", "other@example.com", "password", "password123!"))
                .andExpect(status().isOk()));
        mvc.perform(delete("/posts/" + postId).header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/posts/" + postId).header("Authorization", bearer(owner)))
                .andExpect(status().isOk());
        mvc.perform(post("/posts").header("X-User-Id", "1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restrictsModerationToAdminsUsingCurrentDatabaseRole() throws Exception {
        signup("member@example.com", "지원");
        JsonNode tokens = login();
        mvc.perform(get("/reports").param("status", "PENDING").header("Authorization", bearer(tokens)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        Member member = members.findByEmailAndDeletedAtIsNull("member@example.com").orElseThrow();
        jdbc.update("UPDATE member SET role = 'ADMIN' WHERE id = ?", member.getId());
        // 실제 운영처럼 DB에서 지정한 권한을 다시 읽도록 영속성 컨텍스트를 비웁니다.
        entityManager.clear();
        mvc.perform(get("/reports").param("status", "PENDING").header("Authorization", bearer(tokens)))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsExpiredTamperedAndRefreshBearerTokens() throws Exception {
        signup("member@example.com", "지원");
        JsonNode tokens = login();
        String token = tokens.get("accessToken").asText();
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "." + (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        for (String invalid : new String[]{tampered, tokens.get("refreshToken").asText()}) {
            mvc.perform(get("/reports").param("status", "PENDING").header("Authorization", "Bearer " + invalid))
                    .andExpect(status().isUnauthorized());
        }
        Instant now = Instant.now();
        String expired = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer("blog").subject("1").id(UUID.randomUUID().toString())
                        .issuedAt(now.minusSeconds(1800)).expiresAt(now.minusSeconds(120)).build())).getTokenValue();
        mvc.perform(get("/reports").param("status", "PENDING").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exposesSwaggerBearerAuthenticationAndPublicEndpoints() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string("OK"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        JsonNode api = mapper.readTree(mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertEquals("bearer", api.at("/components/securitySchemes/bearerAuth/scheme").asText());
        assertTrue(api.get("paths").has("/auth"));
        assertTrue(api.get("paths").has("/login"));
        assertTrue(api.get("paths").has("/auth/refresh"));
        assertFalse(api.at("/paths/~1auth/post").has("security"));
        assertEquals("bearerAuth", api.at("/paths/~1posts/post/security/0").fieldNames().next());
        assertFalse(api.at("/paths/~1posts/post").toString().contains("X-User-Id"));
    }

    private ResultActions signup(String email, String nickname) throws Exception {
        return postJson("/auth", Map.of("email", email, "password", "password123!", "nickname", nickname));
    }

    private JsonNode login() throws Exception {
        return data(postJson("/login", Map.of("email", "member@example.com", "password", "password123!"))
                .andExpect(status().isOk()));
    }

    private ResultActions refresh(String token) throws Exception {
        return postJson("/auth/refresh", Map.of("refreshToken", token));
    }

    private ResultActions postJson(String path, Object body) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)));
    }

    private JsonNode data(ResultActions result) throws Exception {
        return mapper.readTree(result.andReturn().getResponse().getContentAsString()).get("data");
    }

    private String bearer(JsonNode tokens) {
        return "Bearer " + tokens.get("accessToken").asText();
    }
}
