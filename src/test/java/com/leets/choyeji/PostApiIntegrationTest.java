package com.leets.choyeji;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.entity.Comment;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:post-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=integration-test-secret-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostApiIntegrationTest {
    private static final String SECRET = "integration-test-secret-32-bytes-long";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PostRepository posts;
    @Autowired MemberRepository members;
    @Autowired CommentRepository comments;
    @Autowired PasswordEncoder passwordEncoder;
    private Member owner;
    private String authorization;

    @BeforeEach
    void setUp() throws Exception {
        comments.deleteAll();
        posts.deleteAll();
        members.deleteAll();
        owner = members.save(new Member(passwordEncoder.encode("test-password"), "홍길동", "owner@example.com"));
        authorization = token(owner.getId(), SECRET, Instant.now().plusSeconds(3600));
    }

    @Test
    void createReadUpdateAndDelete() throws Exception {
        String body = mvc.perform(post("/posts").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"게시글제목\",\"content\":\"게시글내용\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON_200_1"))
                .andExpect(jsonPath("$.data.writer").value("홍길동"))
                .andExpect(jsonPath("$.data.createdDate").value(java.time.LocalDate.now().toString()))
                .andReturn().getResponse().getContentAsString();
        JsonNode created = mapper.readTree(body).get("data");
        long id = created.get("postId").asLong();
        mvc.perform(get("/posts/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("게시글제목"));
        mvc.perform(patch("/posts/{id}", id).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"수정\",\"content\":\"수정 내용\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("수정"))
                .andExpect(jsonPath("$.data.writer").doesNotExist())
                .andExpect(jsonPath("$.data.createdDate").value(created.get("createdDate").asText()));
        mvc.perform(get("/posts/{id}", id)).andExpect(jsonPath("$.data.title").value("수정"));
        comments.save(new Comment(posts.findById(id).orElseThrow(), owner, "댓글"));
        mvc.perform(delete("/posts/{id}", id).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        assertThat(comments.count()).isZero();
        mvc.perform(get("/posts/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_404_1"));
    }

    @Test
    void listIsPaginatedAndSortedNewestFirst() throws Exception {
        Post first = posts.save(new Post(owner, "first", "content"));
        Post second = posts.save(new Post(owner, "second", "content"));
        mvc.perform(get("/posts").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.posts[0].postId").value(second.getId()))
                .andExpect(jsonPath("$.data.page").value(0)).andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2)).andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true));
        mvc.perform(get("/posts").param("page", "1").param("size", "1"))
                .andExpect(jsonPath("$.data.posts[0].postId").value(first.getId()))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void paginationDefaultsAndClamps() throws Exception {
        mvc.perform(get("/posts")).andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10)).andExpect(jsonPath("$.data.posts").isEmpty());
        mvc.perform(get("/posts").param("page", "-1").param("size", "101"))
                .andExpect(jsonPath("$.data.page").value(0)).andExpect(jsonPath("$.data.size").value(100));
        mvc.perform(get("/posts").param("size", "0")).andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    void writesRequireLogin() throws Exception {
        mvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
        mvc.perform(patch("/posts/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/posts/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void anotherMemberCannotUpdateOrDelete() throws Exception {
        long id = posts.save(new Post(owner, "title", "content")).getId();
        Member other = members.save(new Member("password", "other", "other@example.com"));
        String otherToken = token(other.getId(), SECRET, Instant.now().plusSeconds(3600));
        mvc.perform(patch("/posts/{id}", id).header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"changed\",\"content\":\"content\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("POST_403_1"));
        mvc.perform(delete("/posts/{id}", id).header("Authorization", otherToken))
                .andExpect(status().isForbidden());
        assertThat(posts.findById(id).orElseThrow().getTitle()).isEqualTo("title");
    }

    @Test
    void missingPostReturns404ForAllOperations() throws Exception {
        mvc.perform(get("/posts/999999")).andExpect(status().isNotFound());
        mvc.perform(patch("/posts/999999").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POST_404_1"));
        mvc.perform(delete("/posts/999999").header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidInputsUseCommonEnvelope() throws Exception {
        for (String method : new String[]{"POST", "PATCH"}) {
            mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method),
                            method.equals("POST") ? "/posts" : "/posts/1")
                            .header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\" \",\"content\":\"\"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON_400_1"))
                    .andExpect(jsonPath("$.data.title").value("제목을 입력해주세요."))
                    .andExpect(jsonPath("$.data.content").value("내용을 입력해주세요."));
        }
        mvc.perform(get("/posts").param("page", "abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));
        mvc.perform(post("/posts").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON_400_1"));
    }

    @Test
    void invalidExpiredAndUnknownMemberTokensAreRejected() throws Exception {
        for (String bearer : new String[]{"Bearer invalid",
                token(owner.getId(), "wrong-signature-secret-32-bytes-long", Instant.now().plusSeconds(3600)),
                token(owner.getId(), SECRET, Instant.now().minusSeconds(120)),
                token(999999L, SECRET, Instant.now().plusSeconds(3600))}) {
            mvc.perform(post("/posts").header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"title\",\"content\":\"content\"}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
            mvc.perform(patch("/posts/1").header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"title\",\"content\":\"content\"}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
            mvc.perform(delete("/posts/1").header("Authorization", bearer))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
        }
    }

    private String token(Long memberId, String secret, Instant expiry) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
                .subject(memberId.toString()).expirationTime(Date.from(expiry)).build());
        jwt.sign(new MACSigner(secret));
        return "Bearer " + jwt.serialize();
    }

    @Test
    void loginTokenCanCreateUpdateAndDeletePosts() throws Exception {
        String login = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.com\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String bearer = "Bearer " + mapper.readTree(login).path("data").path("accessToken").asText();
        String created = mvc.perform(post("/posts").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.writer").value("홍길동"))
                .andReturn().getResponse().getContentAsString();
        long postId = mapper.readTree(created).path("data").path("postId").asLong();
        assertThat(posts.findById(postId).orElseThrow().getMember().getId()).isEqualTo(owner.getId());
        mvc.perform(patch("/posts/{id}", postId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"updated\",\"content\":\"updated content\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/posts/{id}", postId).header("Authorization", bearer))
                .andExpect(status().isOk());
        assertThat(posts.existsById(postId)).isFalse();
        assertThat(members.count()).isEqualTo(1);
    }

    @Test
    void swaggerDeclaresBearerAuthForWritesAndKeepsReadsPublic() throws Exception {
        String document = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode spec = mapper.readTree(document);
        JsonNode scheme = spec.path("components").path("securitySchemes").path("bearerAuth");
        assertThat(scheme.path("type").asText()).isEqualTo("http");
        assertThat(scheme.path("scheme").asText()).isEqualTo("bearer");
        assertThat(spec.has("security")).isFalse();
        JsonNode paths = spec.path("paths");
        for (String[] operation : new String[][]{
                {"/posts", "post"}, {"/posts/{postId}", "patch"}, {"/posts/{postId}", "delete"},
                {"/posts/{postId}/comments", "post"}, {"/posts/{postId}/reports", "post"},
                {"/admin/reports", "get"}, {"/admin/reports/{id}/resolve", "patch"},
                {"/admin/posts/{postId}/restore", "patch"}}) {
            JsonNode endpoint = paths.path(operation[0]).path(operation[1]);
            assertThat(endpoint.path("security").get(0).has("bearerAuth")).isTrue();
            for (JsonNode parameter : endpoint.path("parameters")) {
                assertThat(parameter.path("name").asText()).isNotEqualTo("Authorization");
            }
        }
        for (String[] operation : new String[][]{
                {"/posts", "get"}, {"/posts/{postId}", "get"},
                {"/posts/{postId}/comments", "get"},
                {"/auth/login", "post"}, {"/auth/signup", "post"}}) {
            assertThat(paths.path(operation[0]).path(operation[1]).has("security")).isFalse();
        }
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
