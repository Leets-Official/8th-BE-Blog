package com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SessionService;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalSessions implements IssueSessionUseCase {
    private final SessionService delegate;

    public TransactionalSessions(SessionService delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public Tokens start(long userId) {
        return delegate.start(userId);
    }
}
