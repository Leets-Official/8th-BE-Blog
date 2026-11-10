package com.leets_8th_be.kanghyeonwoo.auth.application.usecase;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.LoginSessionStore;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.AuthenticationSession;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenFingerprint;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

public class SessionService implements IssueSessionUseCase {
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

    private Tokens rotate(AuthenticationSession session) {
        String refresh = tokens.issue(session.userId(), TokenType.REFRESH, session.id(), REFRESH_LIFETIME);
        session.renew(TokenFingerprint.of(refresh), clock.instant().plus(REFRESH_LIFETIME));
        sessions.save(session);
        String access = tokens.issue(session.userId(), TokenType.ACCESS, session.id(), ACCESS_LIFETIME);
        return new Tokens(access, refresh, ACCESS_LIFETIME.toSeconds());
    }
}
