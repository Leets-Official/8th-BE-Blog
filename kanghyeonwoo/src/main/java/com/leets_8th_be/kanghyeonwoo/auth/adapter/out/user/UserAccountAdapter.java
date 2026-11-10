package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.user;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticationAccount;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.Credentials;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class UserAccountAdapter implements AccountPort {
    private final AccountDirectory accounts;

    public UserAccountAdapter(AccountDirectory accounts) {
        this.accounts = accounts;
    }

    @Override
    public Optional<AuthenticationAccount> byEmail(String email) {
        return accounts.byEmail(email).map(UserAccountAdapter::toAccount);
    }

    @Override
    public Optional<AuthenticationAccount> byKakaoId(String id) {
        return accounts.byKakaoId(id).map(UserAccountAdapter::toAccount);
    }

    @Override
    public Optional<AuthenticationAccount> byId(long id) {
        return Optional.ofNullable(accounts.byId(id)).map(UserAccountAdapter::toAccount);
    }

    @Override
    public boolean emailExists(String email) {
        return accounts.emailExists(email);
    }

    @Override
    public AuthenticationAccount register(String email, String nickname, String passwordHash, String kakaoId) {
        try {
            return toAccount(accounts.register(email, nickname, passwordHash, kakaoId));
        } catch (DataIntegrityViolationException failure) {
            throw new AuthenticationFailure(
                    AuthenticationError.ACCOUNT_CONFLICT, "이미 가입된 이메일 또는 사용 중인 닉네임입니다. 기존 로그인 수단을 확인해 주세요.");
        }
    }

    private static AuthenticationAccount toAccount(Credentials credentials) {
        return new AuthenticationAccount(
                credentials.userId(), credentials.passwordHash(), credentials.kakaoId(), credentials.role());
    }
}
