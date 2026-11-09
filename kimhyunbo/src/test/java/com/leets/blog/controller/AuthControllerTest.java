package com.leets.blog.controller;

import com.jayway.jsonpath.JsonPath;
import com.leets.blog.global.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    // data.sql로 미리 넣어둔 회원 (비밀번호 password1234)
    private static final String MEMBER_EMAIL = "hyunbo@leets.com";
    private static final String MEMBER_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private ResultActions signup(String email, String password, String nickname) throws Exception {
        return mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s", "nickname": "%s"}
                        """.formatted(email, password, nickname)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)));
    }

    private ResultActions reissue(String refreshToken) throws Exception {
        return mockMvc.perform(post("/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(refreshToken)));
    }

    private ResultActions createPost(String accessToken) throws Exception {
        return mockMvc.perform(post("/posts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "제목", "content": "본문"}
                        """));
    }

    private static String read(ResultActions result, String path) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
    }

    @Test
    @DisplayName("회원가입하면 201과 회원 정보를 반환하고, 비밀번호는 응답에 포함하지 않는다")
    void signupSuccess() throws Exception {
        signup("newbie@leets.com", "password1234", "새내기")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("S201"))
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.email").value("newbie@leets.com"))
                .andExpect(jsonPath("$.data.nickname").value("새내기"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("가입한 이메일과 비밀번호로 로그인할 수 있다 (비밀번호는 암호화되어 저장된다)")
    void signupThenLogin() throws Exception {
        signup("newbie@leets.com", "password1234", "새내기").andExpect(status().isCreated());

        login("newbie@leets.com", "password1234")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("이미 가입된 이메일로 가입하면 409와 MEMBER4091을 반환한다")
    void signupWithDuplicatedEmail() throws Exception {
        signup(MEMBER_EMAIL, "password1234", "다른닉네임")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4091"));
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임으로 가입하면 409와 MEMBER4092를 반환한다")
    void signupWithDuplicatedNickname() throws Exception {
        signup("other@leets.com", "password1234", "김현보")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4092"));
    }

    @Test
    @DisplayName("이메일 형식이 잘못되거나 비밀번호가 짧으면 400과 잘못된 필드 목록을 반환한다")
    void signupWithInvalidInput() throws Exception {
        signup("not-an-email", "short", "새내기")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_VALIDATION"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("로그인하면 Access Token과 Refresh Token을 발급하고, Access Token으로 로그인이 필요한 API를 호출할 수 있다")
    void loginSuccess() throws Exception {
        ResultActions result = login(MEMBER_EMAIL, MEMBER_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.grantType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(1800));

        createPost(read(result, "$.data.accessToken"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.authorId").value(101));
    }

    @Test
    @DisplayName("비밀번호가 틀리거나 없는 이메일이면 같은 401(AUTH4011)을 반환한다")
    void loginFailed() throws Exception {
        login(MEMBER_EMAIL, "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4011"));

        login("nobody@leets.com", MEMBER_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4011"));
    }

    @Test
    @DisplayName("Refresh Token으로 새 토큰을 발급받으면, 이전 Refresh Token은 다시 쓸 수 없다")
    void reissue() throws Exception {
        String oldRefreshToken = read(login(MEMBER_EMAIL, MEMBER_PASSWORD), "$.data.refreshToken");

        ResultActions reissued = reissue(oldRefreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());

        // 새로 받은 Access Token은 바로 사용할 수 있다.
        createPost(read(reissued, "$.data.accessToken")).andExpect(status().isCreated());

        // 이미 한 번 쓴 Refresh Token으로는 재발급할 수 없다.
        reissue(oldRefreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4014"));

        // 새로 받은 Refresh Token으로는 다시 재발급할 수 있다.
        reissue(read(reissued, "$.data.refreshToken")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("다시 로그인하면 이전에 받은 Refresh Token은 쓸 수 없다")
    void reissueAfterRelogin() throws Exception {
        String firstRefreshToken = read(login(MEMBER_EMAIL, MEMBER_PASSWORD), "$.data.refreshToken");
        login(MEMBER_EMAIL, MEMBER_PASSWORD).andExpect(status().isOk());

        reissue(firstRefreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4014"));
    }

    @Test
    @DisplayName("Access Token으로는 재발급할 수 없고, Refresh Token으로는 API를 호출할 수 없다")
    void tokenTypesCannotBeSwapped() throws Exception {
        ResultActions loggedIn = login(MEMBER_EMAIL, MEMBER_PASSWORD);

        reissue(read(loggedIn, "$.data.accessToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4012"));

        createPost(read(loggedIn, "$.data.refreshToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4012"));
    }

    @Test
    @DisplayName("만료된 Access Token으로 호출하면 401과 AUTH4013을 반환한다")
    void expiredAccessToken() throws Exception {
        // 유효 시간이 음수인 Provider로, 이미 만료된 토큰을 만든다.
        JwtProvider expiredProvider = new JwtProvider(jwtSecret, Duration.ofSeconds(-1), Duration.ofDays(14));

        createPost(expiredProvider.createAccessToken(101L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH4013"));
    }
}
