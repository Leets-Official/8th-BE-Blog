package com.leets_8th_be.kanghyeonwoo.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.VerifiedToken;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.ReissueTokenUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.LoginSessionStore;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SessionService;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.RefreshTokenReused;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.AuthenticationSession;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SessionUseCaseTest {
    @Test
    void replayRevokesTheSuccessorRefreshToken() {
        AtomicReference<AuthenticationSession> stored = new AtomicReference<>();
        LoginSessionStore sessions = mock(LoginSessionStore.class);
        doAnswer(call -> {
                    stored.set(call.getArgument(0));
                    return null;
                })
                .when(sessions)
                .save(any());
        when(sessions.findForUpdate(anyString())).thenAnswer(call -> Optional.ofNullable(stored.get()));
        TokenProvider tokens = mock(TokenProvider.class);
        when(tokens.issue(anyLong(), eq(TokenType.REFRESH), anyString(), any()))
                .thenReturn("refresh-first", "refresh-next");
        when(tokens.issue(anyLong(), eq(TokenType.ACCESS), anyString(), any())).thenReturn("access");
        Clock clock = Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC);
        SessionService service = new SessionService(sessions, tokens, clock);
        service.start(7);
        when(tokens.verify(anyString(), eq(TokenType.REFRESH)))
                .thenAnswer(call -> new VerifiedToken(7, stored.get().id()));
        ReissueTokenUseCase reissue = service;

        assertThat(reissue.reissue("refresh-first").refreshToken()).isEqualTo("refresh-next");
        assertThatThrownBy(() -> reissue.reissue("refresh-first")).isInstanceOf(RefreshTokenReused.class);
        assertThatThrownBy(() -> reissue.reissue("refresh-next")).isInstanceOf(AuthenticationFailure.class);
    }
}
