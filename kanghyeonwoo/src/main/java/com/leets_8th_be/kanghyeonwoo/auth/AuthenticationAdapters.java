package com.leets_8th_be.kanghyeonwoo.auth;

import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Account;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Accounts;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Passwords;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Session;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Sessions;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.Credentials;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationAdapters {
    @Bean
    Accounts authenticationAccounts(AccountDirectory accounts) {
        return new Accounts() {
            @Override
            public Optional<Account> byEmail(String email) {
                return accounts.byEmail(email).map(AuthenticationAdapters::toAccount);
            }

            @Override
            public Optional<Account> byKakaoId(String id) {
                return accounts.byKakaoId(id).map(AuthenticationAdapters::toAccount);
            }

            @Override
            public boolean emailExists(String email) {
                return accounts.emailExists(email);
            }

            @Override
            public Account register(String email, String nickname, String hash, String kakao) {
                return toAccount(accounts.register(email, nickname, hash, kakao));
            }
        };
    }

    @Bean
    Passwords authenticationPasswords(PasswordEncoder encoder) {
        return new Passwords() {
            @Override
            public String encode(String raw) {
                return encoder.encode(raw);
            }

            @Override
            public boolean matches(String raw, String hash) {
                return encoder.matches(raw, hash);
            }
        };
    }

    @Bean
    Sessions authenticationSessions(LoginSessions repository) {
        return new Sessions() {
            @Override
            public Optional<Session> lock(String id) {
                return repository.lock(id).map(AuthenticationAdapters::toSession);
            }

            @Override
            public void save(Session session) {
                var row = repository.findById(session.id).orElseGet(() -> new LoginSession(session.id, session.userId));
                row.refreshHash = session.refreshHash;
                row.expiresAt = session.expiresAt;
                row.revoked = session.revoked;
                repository.save(row);
            }
        };
    }

    @Bean
    AuthenticationService authenticationService(
            Accounts accounts, Passwords passwords, JwtTokens tokens, Sessions sessions) {
        return new AuthenticationService(accounts, passwords, tokens, sessions);
    }

    private static Session toSession(LoginSession row) {
        Session session = new Session(row.id, row.userId);
        session.refreshHash = row.refreshHash;
        session.expiresAt = row.expiresAt;
        session.revoked = row.revoked;
        return session;
    }

    private static Account toAccount(Credentials credentials) {
        return new Account(credentials.userId(), credentials.passwordHash(), credentials.kakaoId(), credentials.role());
    }
}
