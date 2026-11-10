package com.leets_8th_be.kanghyeonwoo.auth.application.usecase;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Exchange;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.KakaoStart;
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
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.OAuthGrant;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.OAuthPurpose;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenFingerprint;
import java.time.Clock;
import java.time.Duration;

public class KakaoLoginService
        implements StartKakaoLoginUseCase, CompleteKakaoLoginUseCase, ExchangeKakaoCodeUseCase, SignupWithKakaoUseCase {
    private final AccountPort accounts;
    private final OAuthGrantStore grants;
    private final KakaoIdentityProvider provider;
    private final IssueSessionUseCase sessions;
    private final OpaqueTokenGenerator random;
    private final Clock clock;

    public KakaoLoginService(
            AccountPort accounts,
            OAuthGrantStore grants,
            KakaoIdentityProvider provider,
            IssueSessionUseCase sessions,
            OpaqueTokenGenerator random,
            Clock clock) {
        this.accounts = accounts;
        this.grants = grants;
        this.provider = provider;
        this.sessions = sessions;
        this.random = random;
        this.clock = clock;
    }

    @Override
    public KakaoStart start() {
        String browser = random.generate();
        String state = random.generate();
        grants.save(newGrant(state, OAuthPurpose.STATE, browser, Duration.ofMinutes(5)));
        return new KakaoStart(state, browser);
    }

    @Override
    public String complete(String state, String browser, String authorizationCode) {
        consume(state, OAuthPurpose.STATE, browser);
        var identity = provider.identity(authorizationCode);
        var existing = accounts.byKakaoId(identity.id());
        if (existing.isEmpty() && (identity.email() == null || identity.email().isBlank())) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_INPUT);
        }
        if (existing.isEmpty() && accounts.emailExists(identity.email())) {
            throw new AuthenticationFailure(AuthenticationError.ACCOUNT_CONFLICT);
        }
        String exchangeCode = random.generate();
        OAuthGrant exchange = newGrant(exchangeCode, OAuthPurpose.EXCHANGE, browser, Duration.ofSeconds(60))
                .withIdentity(
                        identity.id(),
                        identity.email(),
                        existing.map(account -> account.userId()).orElse(null));
        grants.save(exchange);
        return exchangeCode;
    }

    @Override
    public Exchange exchange(String code, String browser) {
        OAuthGrant exchange = consume(code, OAuthPurpose.EXCHANGE, browser);
        if (exchange.userId() != null) {
            return new Exchange(sessions.start(exchange.userId()), null);
        }
        String signupToken = random.generate();
        OAuthGrant signup = newGrant(signupToken, OAuthPurpose.SIGNUP, browser, Duration.ofMinutes(5))
                .withIdentity(exchange.kakaoId(), exchange.email(), null);
        grants.save(signup);
        return new Exchange(null, signupToken);
    }

    @Override
    public Tokens signup(String token, String browser, String nickname) {
        OAuthGrant signup = consume(token, OAuthPurpose.SIGNUP, browser);
        if (accounts.emailExists(signup.email())
                || accounts.byKakaoId(signup.kakaoId()).isPresent()) {
            throw new AuthenticationFailure(AuthenticationError.ACCOUNT_CONFLICT);
        }
        var account = accounts.register(signup.email(), nickname.trim(), null, signup.kakaoId());
        return sessions.start(account.userId());
    }

    private OAuthGrant newGrant(String raw, OAuthPurpose purpose, String browser, Duration lifetime) {
        return new OAuthGrant(
                TokenFingerprint.of(raw),
                purpose,
                TokenFingerprint.of(browser),
                clock.instant().plus(lifetime),
                null,
                null,
                null);
    }

    private OAuthGrant consume(String raw, OAuthPurpose purpose, String browser) {
        if (raw == null || browser == null) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
        OAuthGrant grant = grants.findForUpdate(TokenFingerprint.of(raw))
                .orElseThrow(() -> new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS));
        grant.requireUsable(purpose, TokenFingerprint.of(browser), clock.instant());
        grants.delete(grant);
        return grant;
    }
}
