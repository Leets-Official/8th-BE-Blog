package com.example.demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 통합 테스트: 실제 스프링 서버 구성(보안 필터 + Controller + Service + H2 DB)을 띄우고
// 가짜 HTTP 요청(MockMvc)을 보내 처음부터 끝까지 확인한다
@SpringBootTest
class AuthIntegrationTest {

    @Autowired WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())   // 보안 필터 체인까지 거치게 한다
                .build();
    }

    // ---------- 도우미 메서드 ----------

    private void signup(String email, String nickname) throws Exception {
        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password1234","nickname":"%s","name":"용정현","tel":"01012345678"}
                                """.formatted(email, nickname)))
                .andExpect(status().isCreated());
    }

    // 로그인해서 응답 JSON 전체를 문자열로 돌려준다
    private String login(String email) throws Exception {
        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password1234"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String accessTokenOf(String loginJson) {
        return JsonPath.read(loginJson, "$.result.accessToken");
    }

    private String refreshTokenOf(String loginJson) {
        return JsonPath.read(loginJson, "$.result.refreshToken");
    }

    private String reissueBody(String refreshToken) {
        return """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
    }

    // ---------- 회원가입 ----------

    @Test
    @DisplayName("회원가입: 성공하면 201, 같은 이메일로 다시 가입하면 409 MEMBER4091")
    void signup_duplicateEmail() throws Exception {
        signup("dup@leets.com", "dupNick");

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"dup@leets.com","password":"password1234","nickname":"other","name":"용정현","tel":"01012345678"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4091"));
    }

    @Test
    @DisplayName("회원가입: 비밀번호 규칙(영문+숫자 8~20자)을 어기면 400 MEMBER4004")
    void signup_invalidPassword() throws Exception {
        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"weak@leets.com","password":"1234","nickname":"weak","name":"용정현","tel":"01012345678"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEMBER4004"));
    }

    // ---------- 로그인 ----------

    @Test
    @DisplayName("로그인: 비밀번호가 틀리면 401 AUTH4011")
    void login_wrongPassword() throws Exception {
        signup("login@leets.com", "loginNick");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@leets.com","password":"wrong12345"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4011"));
    }

    // ---------- 인증·인가 ----------

    @Test
    @DisplayName("게시글 작성: 토큰 없으면 401, 토큰 있으면 201, 조회는 토큰 없이도 200")
    void createPost_needsToken() throws Exception {
        signup("writer@leets.com", "writer");
        String accessToken = accessTokenOf(login("writer@leets.com"));
        String body = """
                {"title":"제목","content":"본문"}
                """;

        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));

        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("위조된 토큰이면 401 AUTH4012")
    void forgedToken() throws Exception {
        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"제목","content":"본문"}
                                """)
                        .header("Authorization", "Bearer this.is.fake"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4012"));
    }

    @Test
    @DisplayName("남의 게시글 삭제: 로그인은 했어도 작성자가 아니면 403 POST4031")
    void deleteOthersPost_forbidden() throws Exception {
        signup("owner@leets.com", "owner");
        signup("stranger@leets.com", "stranger");
        String ownerToken = accessTokenOf(login("owner@leets.com"));
        String strangerToken = accessTokenOf(login("stranger@leets.com"));

        String created = mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"내 글","content":"본문"}
                                """)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number postId = JsonPath.read(created, "$.result.postId");

        mockMvc.perform(delete("/posts/" + postId).header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST4031"));
    }

    // ---------- 재발급 ----------

    @Test
    @DisplayName("재발급: 새 토큰을 받으면, 이미 쓴 옛 리프레시 토큰은 거절되고 새 것까지 폐기된다(회전 + 재사용 감지)")
    void reissue_rotationAndReuseDetection() throws Exception {
        signup("rotate@leets.com", "rotate");
        String oldRefresh = refreshTokenOf(login("rotate@leets.com"));

        // 1) 정상 재발급
        String reissued = mockMvc.perform(post("/auth/reissue").contentType(MediaType.APPLICATION_JSON)
                        .content(reissueBody(oldRefresh)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newRefresh = refreshTokenOf(reissued);

        // 2) 옛 토큰 재사용 → 거절
        mockMvc.perform(post("/auth/reissue").contentType(MediaType.APPLICATION_JSON)
                        .content(reissueBody(oldRefresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4014"));

        // 3) 재사용이 감지됐으므로 새 토큰도 더 이상 쓸 수 없다 → 다시 로그인해야 함
        mockMvc.perform(post("/auth/reissue").contentType(MediaType.APPLICATION_JSON)
                        .content(reissueBody(newRefresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4014"));
    }

    @Test
    @DisplayName("리프레시 토큰으로는 일반 API를 호출할 수 없다")
    void refreshTokenCannotCallApi() throws Exception {
        signup("type@leets.com", "typeNick");
        String refreshToken = refreshTokenOf(login("type@leets.com"));

        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"제목","content":"본문"}
                                """)
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }
}
