package com.example.demo.auth;

import com.example.demo.domain.SocialAccount;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.SocialAccountRepository;
import com.example.demo.service.KakaoLoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:kakao-test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(KakaoLoginIntegrationTests.KakaoProviderStub.class)
class KakaoLoginIntegrationTests {
    private static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/kakao";
    private static final String CALLBACK = "/login/oauth2/code/kakao";

    @Autowired MockMvc mvc;
    @Autowired MockRestServiceServer provider;
    @Autowired ObjectMapper mapper;
    @Autowired SocialAccountRepository accounts;
    @Autowired MemberRepository members;
    @Autowired JwtDecoder jwt;
    @Autowired KakaoLoginService login;

    @AfterEach
    void verifyProviderCalls() {
        try {
            provider.verify();
        } finally {
            provider.reset();
        }
    }

    @Test
    void firstLoginCreatesMemberAndLaterLoginReusesIt() throws Exception {
        OAuthFlow first = start();
        expectToken("first-code");
        expectProfile(1001);
        TokenResponse firstTokens = tokens(callback(first, "first-code")
                .andExpect(status().isOk()).andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store")));

        provider.verify();
        provider.reset();
        OAuthFlow second = start();
        expectToken("second-code");
        expectProfile(1001);
        TokenResponse secondTokens = tokens(callback(second, "second-code").andExpect(status().isOk()));

        var linked = accounts.findByProviderAndProviderUserId(SocialAccount.Provider.KAKAO, "1001").orElseThrow();
        String memberId = linked.getMember().getId().toString();
        assertThat(jwt.decode(firstTokens.accessToken()).getSubject()).isEqualTo(memberId);
        assertThat(jwt.decode(secondTokens.accessToken()).getSubject()).isEqualTo(memberId);
        assertThat(secondTokens.refreshToken()).isNotEqualTo(firstTokens.refreshToken());

        mvc.perform(post("/posts").header(HttpHeaders.AUTHORIZATION, "Bearer " + firstTokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"카카오 회원의 글","content":"본문"}
                                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value(Long.parseLong(memberId)));
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(java.util.Map.of("refreshToken", firstTokens.refreshToken()))))
                .andExpect(status().isOk());
    }

    @Test
    void requiresMatchingBrowserStateAndConsumesItOnCancellation() throws Exception {
        OAuthFlow flow = start();
        mvc.perform(get(CALLBACK).param("state", flow.state()).param("code", "untrusted"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(CALLBACK).session(flow.session()).param("state", "wrong").param("code", "untrusted"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(CALLBACK).session(flow.session()).param("state", flow.state()).param("error", "access_denied"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_KAKAO_CALLBACK"));
        callback(flow, "untrusted").andExpect(status().isBadRequest());
    }

    @Test
    void failedTokenExchangeDoesNotCallProfileOrAcceptCallbackReplay() throws Exception {
        OAuthFlow flow = start();
        provider.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        callback(flow, "invalid-code").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("KAKAO_LOGIN_FAILED"));
        callback(flow, "invalid-code").andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidProviderProfile() throws Exception {
        OAuthFlow flow = start();
        expectToken("profile-code");
        provider.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        callback(flow, "profile-code").andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("KAKAO_PROVIDER_ERROR"));
    }

    @Test
    void concurrentFirstLoginCreatesOnlyOneMember() throws Exception {
        long before = members.count();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var one = pool.submit(() -> {
                start.await();
                return login.login(1002);
            });
            var two = pool.submit(() -> {
                start.await();
                return login.login(1002);
            });
            start.countDown();
            assertThat(jwt.decode(one.get(10, TimeUnit.SECONDS).accessToken()).getSubject())
                    .isEqualTo(jwt.decode(two.get(10, TimeUnit.SECONDS).accessToken()).getSubject());
        }
        assertThat(members.count()).isEqualTo(before + 1);
    }

    private OAuthFlow start() throws Exception {
        var redirect = mvc.perform(get("/auth/kakao"))
                .andExpect(status().isFound()).andReturn();
        String location = redirect.getResponse().getHeader(HttpHeaders.LOCATION);
        var url = UriComponentsBuilder.fromUriString(location).build();
        assertThat(url.getHost()).isEqualTo("kauth.kakao.com");
        assertThat(url.getQueryParams().getFirst("client_id")).isEqualTo("kakao-test-id");
        assertThat(location).doesNotContain("kakao-test-secret");
        String state = url.getQueryParams().getFirst("state");
        assertThat(state).isNotBlank();
        return new OAuthFlow((MockHttpSession) redirect.getRequest().getSession(false), state);
    }

    private ResultActions callback(OAuthFlow flow, String code) throws Exception {
        return mvc.perform(get(CALLBACK).session(flow.session()).param("state", flow.state()).param("code", code));
    }

    private TokenResponse tokens(ResultActions result) throws Exception {
        return mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), TokenResponse.class);
    }

    private void expectToken(String code) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", "kakao-test-id");
        form.add("client_secret", "kakao-test-secret");
        form.add("redirect_uri", REDIRECT_URI);
        form.add("code", code);
        provider.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST)).andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().formData(form))
                .andRespond(withSuccess("""
                        {"access_token":"provider-token"}
                        """, MediaType.APPLICATION_JSON));
    }

    private void expectProfile(long id) {
        provider.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET)).andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer provider-token"))
                .andRespond(withSuccess("{\"id\":" + id + "}", MediaType.APPLICATION_JSON));
    }

    private record OAuthFlow(MockHttpSession session, String state) {}

    @TestConfiguration
    static class KakaoProviderStub {
        private final RestClient.Builder builder = RestClient.builder();

        @Bean
        MockRestServiceServer provider() {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean
        @Primary
        KakaoOAuthClient mockProviderClient(MockRestServiceServer provider) {
            return new KakaoOAuthClient(builder.build(), "kakao-test-id", "kakao-test-secret", REDIRECT_URI);
        }
    }
}
