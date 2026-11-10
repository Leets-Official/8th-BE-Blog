package com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalSignup implements SignupUseCase {
    private final SignupUseCase delegate;

    public TransactionalSignup(SignupUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public long signup(String email, String password, String nickname) {
        return delegate.signup(email, password, nickname);
    }
}
