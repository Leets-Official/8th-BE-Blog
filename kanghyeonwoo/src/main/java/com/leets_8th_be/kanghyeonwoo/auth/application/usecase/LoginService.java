package com.leets_8th_be.kanghyeonwoo.auth.application.usecase;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.EmailNormalization;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoginService implements LoginUseCase {
    private final AccountPort accounts;
    private final PasswordHasher passwords;
    private final IssueSessionUseCase sessions;
    private final String dummyHash;

    public LoginService(AccountPort accounts, PasswordHasher passwords, IssueSessionUseCase sessions) {
        this.accounts = accounts;
        this.passwords = passwords;
        this.sessions = sessions;
        dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    @Override
    public Tokens login(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
        var account = accounts.byEmail(EmailNormalization.normalize(email)).orElse(null);
        String hash = account == null || account.passwordHash() == null ? dummyHash : account.passwordHash();
        boolean valid = passwords.matches(password, hash);
        if (!valid || account == null || account.passwordHash() == null) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
        return sessions.start(account.userId());
    }
}
