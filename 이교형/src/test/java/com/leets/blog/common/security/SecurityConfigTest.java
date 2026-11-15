package com.leets.blog.common.security;

import com.leets.blog.common.security.jwt.JwtProperties;
import com.leets.blog.common.security.jwt.JwtProvider;
import com.leets.blog.domain.member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 인가 규칙(공개·보호·관리자)과 401·403 응답 형식 검증 (로그인 API 없이 JwtProvider로 직접 토큰 발급)
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired MockMvc mvc;
    @Autowired JwtProvider jwtProvider;
    @Autowired JwtProperties jwtProperties;

    private ResultActions createPost(String token) throws Exception {
        var request = post("/posts").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"t\",\"content\":\"c\"}");
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return mvc.perform(request);
    }

    @Test
    void 공개_API는_토큰_없이_호출된다() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk());
        mvc.perform(get("/posts")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void 공개_API는_잘못된_토큰이_붙어_와도_그대로_호출된다() throws Exception {
        mvc.perform(get("/posts").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isOk());
    }

    @Test
    void 보호_API에_토큰이_없으면_팀_응답_형식의_401() throws Exception {
        createPost(null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void 보호_API에_위조_만료_Refresh_토큰이면_사유별_401() throws Exception {
        String expired = new JwtProvider(new JwtProperties(
                jwtProperties.secret(), Duration.ofSeconds(-1), jwtProperties.refreshTokenValidity()))
                .createAccessToken(1L, MemberRole.MEMBER);

        createPost("abc.def.ghi").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
        createPost(expired).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("EXPIRED_TOKEN"));
        createPost(jwtProvider.createRefreshToken(1L)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    void 일반_회원이_관리자_API를_호출하면_403() throws Exception {
        String memberToken = jwtProvider.createAccessToken(1L, MemberRole.MEMBER);

        mvc.perform(patch("/posts/1/hide").header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_ONLY"));
        mvc.perform(patch("/reports/1/resolve").header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_API에_토큰이_없으면_403이_아니라_401() throws Exception {
        mvc.perform(patch("/posts/1/hide"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
