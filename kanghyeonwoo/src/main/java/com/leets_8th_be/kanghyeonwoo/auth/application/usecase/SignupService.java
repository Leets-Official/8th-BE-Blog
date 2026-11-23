package com.leets_8th_be.kanghyeonwoo.auth.application.usecase;

import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.EmailNormalization;
import java.nio.charset.StandardCharsets;

public class SignupService implements SignupUseCase {
    private final AccountPort accounts;
    private final PasswordHasher passwords;

    public SignupService(AccountPort accounts, PasswordHasher passwords) {
        this.accounts = accounts;
        this.passwords = passwords;
    }

    @Override
    public long signup(String email, String password, String nickname) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_INPUT);
        }
        String normalizedEmail = EmailNormalization.normalize(email);
        String passwordHash = passwords.encode(password);
        return accounts.register(normalizedEmail, nickname.trim(), passwordHash, null)
                .userId();
    }
}
