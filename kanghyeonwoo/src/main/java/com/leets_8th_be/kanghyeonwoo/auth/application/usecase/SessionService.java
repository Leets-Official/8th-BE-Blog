package com.leets_8th_be.kanghyeonwoo.auth.application.usecase;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LogoutUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.ReissueTokenUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.LoginSessionStore;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.RefreshTokenReused;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.AuthenticationSession;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenFingerprint;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

public class SessionService implements IssueSessionUseCase, ReissueTokenUseCase, LogoutUseCase {
    private static final Duration ACCESS_LIFETIME = Duration.ofMinutes(10);
    private static final Duration REFRESH_LIFETIME = Duration.ofDays(7);
    private final LoginSessionStore sessions;
    private final TokenProvider tokens;
    private final Clock clock;

    public SessionService(LoginSessionStore sessions, TokenProvider tokens, Clock clock) {
        this.sessions = sessions;
        this.tokens = tokens;
        this.clock = clock;
    }

    @Override
    public Tokens start(long userId) {
        AuthenticationSession session =
                new AuthenticationSession(UUID.randomUUID().toString(), userId);
        return rotate(session);
    }

    @Override
    public Tokens reissue(String refreshToken) {
        var verified = tokens.verify(refreshToken, TokenType.REFRESH);
        var session = sessions.findForUpdate(verified.sessionId())
                .orElseThrow(() -> new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS));
        session.requireUsable(verified.userId(), clock.instant());
        try {
            session.requireRefreshHash(TokenFingerprint.of(refreshToken));
        } catch (RefreshTokenReused failure) {
            sessions.save(session);
            throw failure;
        }
        return rotate(session);
    }

    @Override
    public void logout(String refreshToken) {
        var verified = tokens.verify(refreshToken, TokenType.REFRESH);
        var session = sessions.findForUpdate(verified.sessionId())
                .orElseThrow(() -> new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS));
        session.requireOwner(verified.userId());
        session.revoke();
        sessions.save(session);
    }

    private Tokens rotate(AuthenticationSession session) {
        String refresh = tokens.issue(session.userId(), TokenType.REFRESH, session.id(), REFRESH_LIFETIME);
        session.renew(TokenFingerprint.of(refresh), clock.instant().plus(REFRESH_LIFETIME));
        sessions.save(session);
        String access = tokens.issue(session.userId(), TokenType.ACCESS, session.id(), ACCESS_LIFETIME);
        return new Tokens(access, refresh, ACCESS_LIFETIME.toSeconds());
    }
}
