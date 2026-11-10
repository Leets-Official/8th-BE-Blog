package com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LoginUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalLogin implements LoginUseCase {
    private final LoginUseCase delegate;

    public TransactionalLogin(LoginUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public Tokens login(String email, String password) {
        return delegate.login(email, password);
    }
}
