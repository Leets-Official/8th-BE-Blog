package com.leets_8th_be.kanghyeonwoo.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticationAccount;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.Exchange;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.KakaoIdentity;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.CompleteKakaoLoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.ExchangeKakaoCodeUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupWithKakaoUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.StartKakaoLoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.KakaoIdentityProvider;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.OAuthGrantStore;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.OpaqueTokenGenerator;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.KakaoLoginService;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.OAuthGrant;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class KakaoUseCaseTest {
    @Test
    void browserBoundOneTimeCodeMustBeExchangedBeforeSignup() {
        AccountPort accounts = mock(AccountPort.class);
        when(accounts.register("kakao@example.com", "Nickname", null, "kakao-id"))
                .thenReturn(new AuthenticationAccount(7, null, "kakao-id", "USER"));
        KakaoIdentityProvider provider = mock(KakaoIdentityProvider.class);
        when(provider.identity("provider-code")).thenReturn(new KakaoIdentity("kakao-id", "kakao@example.com"));
        OpaqueTokenGenerator random = mock(OpaqueTokenGenerator.class);
        when(random.generate()).thenReturn("browser", "state", "exchange", "signup");
        IssueSessionUseCase sessions = mock(IssueSessionUseCase.class);
        Tokens tokens = new Tokens("access", "refresh", 600);
        when(sessions.start(7)).thenReturn(tokens);
        var flow = new KakaoLoginService(
                accounts,
                new MemoryGrants(),
                provider,
                sessions,
                random,
                Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC));
        StartKakaoLoginUseCase start = flow;
        CompleteKakaoLoginUseCase complete = flow;
        ExchangeKakaoCodeUseCase exchange = flow;
        SignupWithKakaoUseCase signup = flow;

        var started = start.start();
        String code = complete.complete(started.state(), started.browser(), "provider-code");
        assertThatThrownBy(() -> exchange.exchange(code, "other-browser")).isInstanceOf(AuthenticationFailure.class);
        Exchange result = exchange.exchange(code, started.browser());
        assertThat(result.tokens()).isNull();
        assertThatThrownBy(() -> exchange.exchange(code, started.browser())).isInstanceOf(AuthenticationFailure.class);
        assertThat(signup.signup(result.signupToken(), started.browser(), " Nickname "))
                .isEqualTo(tokens);
    }

    private static class MemoryGrants implements OAuthGrantStore {
        private final Map<String, OAuthGrant> grants = new HashMap<>();

        public Optional<OAuthGrant> findForUpdate(String hash) {
            return Optional.ofNullable(grants.get(hash));
        }

        public void save(OAuthGrant grant) {
            grants.put(grant.hash(), grant);
        }

        public void delete(OAuthGrant grant) {
            grants.remove(grant.hash());
        }
    }
}
