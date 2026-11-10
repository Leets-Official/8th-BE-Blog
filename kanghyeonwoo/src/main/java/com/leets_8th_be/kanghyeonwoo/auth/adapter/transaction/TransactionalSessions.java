package com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LogoutUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.ReissueTokenUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SessionService;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.RefreshTokenReused;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalSessions implements IssueSessionUseCase, ReissueTokenUseCase, LogoutUseCase {
    private final SessionService delegate;

    public TransactionalSessions(SessionService delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public Tokens start(long userId) {
        return delegate.start(userId);
    }

    @Override
    @Transactional(noRollbackFor = RefreshTokenReused.class)
    public Tokens reissue(String refreshToken) {
        return delegate.reissue(refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        delegate.logout(refreshToken);
    }
}
