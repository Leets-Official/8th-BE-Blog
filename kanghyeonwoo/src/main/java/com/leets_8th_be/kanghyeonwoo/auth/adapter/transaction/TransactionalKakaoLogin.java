package com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Exchange;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.KakaoStart;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.CompleteKakaoLoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.ExchangeKakaoCodeUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupWithKakaoUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.StartKakaoLoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.KakaoLoginService;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalKakaoLogin
        implements StartKakaoLoginUseCase, CompleteKakaoLoginUseCase, ExchangeKakaoCodeUseCase, SignupWithKakaoUseCase {
    private final KakaoLoginService delegate;

    public TransactionalKakaoLogin(KakaoLoginService delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public KakaoStart start() {
        return delegate.start();
    }

    @Override
    @Transactional(noRollbackFor = AuthenticationFailure.class)
    public String complete(String state, String browser, String authorizationCode) {
        return delegate.complete(state, browser, authorizationCode);
    }

    @Override
    @Transactional
    public Exchange exchange(String code, String browser) {
        return delegate.exchange(code, browser);
    }

    @Override
    @Transactional
    public Tokens signup(String token, String browser, String nickname) {
        return delegate.signup(token, browser, nickname);
    }
}
