package com.example.demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 회원가입 / 로그인 / 토큰 검증 / 재발급 통합 테스트
@SpringBootTest(properties = "jwt.secret=test-secret-key-for-leets-week6-jwt-0123456789")
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    MockMvc mvc;

    static final String SIGNUP = """
            {"email":"tester@test.com","password":"password1234!","nickname":"tester"}""";
    static final String LOGIN = """
            {"email":"tester@test.com","password":"password1234!"}""";

    // ---------- 회원가입 ----------

    @Test
    void 회원가입에_성공하면_201() throws Exception {
        postJson("/auth/signup", SIGNUP)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("AUTH_201_1"))
                .andExpect(jsonPath("$.data.email").value("tester@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"));
    }

    @Test
    void 이메일은_대소문자를_구분하지_않고_중복을_비교한다() throws Exception {
        postJson("/auth/signup", SIGNUP).andExpect(status().isCreated());

        postJson("/auth/signup", """
                {"email":"TESTER@Test.com","password":"password1234!","nickname":"other"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_409_1"));
    }

    @Test
    void 닉네임이_중복이면_409() throws Exception {
        postJson("/auth/signup", SIGNUP).andExpect(status().isCreated());

        postJson("/auth/signup", """
                {"email":"other@test.com","password":"password1234!","nickname":"tester"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_409_2"));
    }

    @Test
    void 이메일_형식이_틀리면_400() throws Exception {
        postJson("/auth/signup", """
                {"email":"not-an-email","password":"password1234!","nickname":"tester"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void 비밀번호가_8자_미만이면_400() throws Exception {
        postJson("/auth/signup", """
                {"email":"tester@test.com","password":"short","nickname":"tester"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    void 비밀번호가_72바이트를_넘으면_400() throws Exception {
        String longKorean = "가".repeat(25); // 25자 = 75바이트
        postJson("/auth/signup", """
                {"email":"tester@test.com","password":"%s","nickname":"tester"}""".formatted(longKorean))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_1"));
    }

    // ---------- 로그인 ----------

    @Test
    void 로그인에_성공하면_두_토큰이_발급된다() throws Exception {
        postJson("/auth/signup", SIGNUP).andExpect(status().isCreated());

        postJson("/auth/login", LOGIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("AUTH_200_1"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void 비밀번호가_틀리면_401() throws Exception {
        postJson("/auth/signup", SIGNUP).andExpect(status().isCreated());

        postJson("/auth/login", LOGIN.replace("password1234!", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_1"));
    }

    @Test
    void 가입하지_않은_이메일도_같은_401로_응답한다() throws Exception {
        postJson("/auth/login", LOGIN)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_1"));
    }

    // ---------- 토큰 검증 (보호된 API: GET /users/me) ----------

    @Test
    void 인증_정보가_없으면_401() throws Exception {
        mvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_2"));
    }

    @Test
    void 유효한_Access_Token이면_내_정보를_조회한다() throws Exception {
        String access = signupAndLogin("accessToken");

        me(access)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("tester"));
    }

    @Test
    void 변조된_토큰은_401() throws Exception {
        String access = signupAndLogin("accessToken");

        int signatureStart = access.lastIndexOf('.') + 1;
        char changed = access.charAt(signatureStart) == 'A' ? 'B' : 'A';
        String tampered = access.substring(0, signatureStart) + changed + access.substring(signatureStart + 1);

        me(tampered)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_2"));
    }

    @Test
    void Refresh_Token으로_일반_API를_호출할_수_없다() throws Exception {
        String refresh = signupAndLogin("refreshToken");

        me(refresh).andExpect(status().isUnauthorized());
    }

    // ---------- 재발급 ----------

    @Test
    void 재발급에_성공하면_새_토큰을_받는다() throws Exception {
        String refresh = signupAndLogin("refreshToken");

        String body = reissue(refresh)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("AUTH_200_2"))
                .andReturn().getResponse().getContentAsString();

        String newAccess = JsonPath.read(body, "$.data.accessToken");
        me(newAccess).andExpect(status().isOk());
    }

    @Test
    void 이미_사용한_Refresh_Token을_재사용하면_거절되고_재발급_권한이_폐기된다() throws Exception {
        String first = signupAndLogin("refreshToken");

        String body = reissue(first).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String second = JsonPath.read(body, "$.data.refreshToken");

        // 이미 회전된 토큰 재사용 -> 거절
        reissue(first).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_3"));
        // 연결된 재발급 권한도 폐기 -> 최신 토큰도 거절 (다시 로그인 필요)
        reissue(second).andExpect(status().isUnauthorized());
    }

    @Test
    void Access_Token으로_재발급할_수_없다() throws Exception {
        String access = signupAndLogin("accessToken");

        reissue(access).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_3"));
    }

    // ---------- helpers ----------

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions me(String accessToken) throws Exception {
        return mvc.perform(get("/users/me").header("Authorization", "Bearer " + accessToken));
    }

    private ResultActions reissue(String refreshToken) throws Exception {
        return postJson("/auth/reissue", "{\"refreshToken\":\"" + refreshToken + "\"}");
    }

    // 가입 + 로그인 후 응답의 data.{field} (accessToken / refreshToken) 를 꺼낸다.
    private String signupAndLogin(String field) throws Exception {
        postJson("/auth/signup", SIGNUP).andExpect(status().isCreated());
        String body = postJson("/auth/login", LOGIN).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data." + field);
    }
}
