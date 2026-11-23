package com.leets_8th_be.kanghyeonwoo.auth;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.KakaoIdentity;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.KakaoIdentityProvider;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "auth.kakao.client-id=test-app",
            "auth.kakao.callback=http://localhost/api/v2/auth/kakao/callback",
            "auth.kakao.frontend=https://frontend.example/callback"
        })
@AutoConfigureMockMvc
class KakaoApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @MockitoBean
    KakaoIdentityProvider client;

    @Test
    void newMemberCompletesSignupAndExchangeCannotBeReplayed() throws Exception {
        when(client.identity("valid-code")).thenReturn(new KakaoIdentity("12345", "kakao@example.com"));
        var start = mvc.perform(get("/api/v2/auth/kakao/start"))
                .andExpect(status().isFound())
                .andReturn()
                .getResponse();
        String state = start.getRedirectedUrl().split("state=")[1];
        Cookie browser = start.getCookie("kakao_browser");
        mvc.perform(get("/api/v2/auth/kakao/callback").param("state", state).param("code", "valid-code"))
                .andExpect(status().isUnauthorized());
        var callback = mvc.perform(get("/api/v2/auth/kakao/callback")
                        .cookie(browser)
                        .param("state", state)
                        .param("code", "valid-code"))
                .andExpect(status().isFound())
                .andReturn()
                .getResponse();
        String code = callback.getRedirectedUrl().split("code=")[1];
        String body = json.writeValueAsString(Map.of("code", code));
        var exchanged = mvc.perform(post("/api/v2/auth/kakao/exchange")
                        .cookie(browser)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        String signup = json.readTree(exchanged.getResponse().getContentAsString())
                .path("data")
                .path("signupToken")
                .asText();
        mvc.perform(post("/api/v2/auth/kakao/exchange")
                        .cookie(browser)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v2/auth/kakao/signup")
                        .cookie(browser)
                        .contentType("application/json")
                        .content(json.writeValueAsString(Map.of("signupToken", signup, "nickname", "카카오회원"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());
        var nextStart = mvc.perform(get("/api/v2/auth/kakao/start"))
                .andExpect(status().isFound())
                .andReturn()
                .getResponse();
        var nextBrowser = nextStart.getCookie("kakao_browser");
        var nextCallback = mvc.perform(get("/api/v2/auth/kakao/callback")
                        .cookie(nextBrowser)
                        .param("state", nextStart.getRedirectedUrl().split("state=")[1])
                        .param("code", "valid-code"))
                .andExpect(status().isFound())
                .andReturn()
                .getResponse();
        mvc.perform(post("/api/v2/auth/kakao/exchange")
                        .cookie(nextBrowser)
                        .contentType("application/json")
                        .content(json.writeValueAsString(
                                Map.of("code", nextCallback.getRedirectedUrl().split("code=")[1]))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokens.accessToken").isString())
                .andExpect(jsonPath("$.data.signupToken").isEmpty());
    }

    @Test
    void duplicateEmailWarnsToUseExistingLoginAndMissingEmailCannotSignup() throws Exception {
        mvc.perform(
                        post("/api/v2/auth/signup")
                                .contentType("application/json")
                                .content(
                                        "{\"email\":\"existing@example.com\",\"password\":\"password123\",\"nickname\":\"existing-user\"}"))
                .andExpect(status().isCreated());
        when(client.identity("duplicate")).thenReturn(new KakaoIdentity("duplicate-id", "existing@example.com"));
        when(client.identity("missing")).thenReturn(new KakaoIdentity("missing-id", null));
        for (String code : List.of("duplicate", "missing")) {
            var start = mvc.perform(get("/api/v2/auth/kakao/start"))
                    .andExpect(status().isFound())
                    .andReturn()
                    .getResponse();
            var result = mvc.perform(get("/api/v2/auth/kakao/callback")
                    .cookie(start.getCookie("kakao_browser"))
                    .param("state", start.getRedirectedUrl().split("state=")[1])
                    .param("code", code));
            if (code.equals("duplicate")) {
                result.andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value("이미 가입된 계정이 있습니다. 기존 로그인 수단을 이용해 주세요."));
            } else {
                result.andExpect(status().isBadRequest());
            }
        }
    }
}
