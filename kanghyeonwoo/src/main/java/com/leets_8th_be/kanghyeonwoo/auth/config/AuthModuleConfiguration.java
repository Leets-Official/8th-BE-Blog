package com.leets_8th_be.kanghyeonwoo.auth.config;

import com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction.TransactionalLogin;
import com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction.TransactionalSessions;
import com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction.TransactionalSignup;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.AuthenticateAccessTokenUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.LoginSessionStore;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.AccessTokenAuthenticationService;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.LoginService;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SessionService;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SignupService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthModuleConfiguration {
    @Bean
    SignupUseCase signupUseCase(AccountPort accounts, PasswordHasher passwords) {
        return new TransactionalSignup(new SignupService(accounts, passwords));
    }

    @Bean
    TransactionalSessions tokenSessionUseCases(LoginSessionStore sessions, TokenProvider tokens) {
        return new TransactionalSessions(new SessionService(sessions, tokens, Clock.systemUTC()));
    }

    @Bean
    LoginUseCase loginUseCase(AccountPort accounts, PasswordHasher passwords, IssueSessionUseCase sessions) {
        return new TransactionalLogin(new LoginService(accounts, passwords, sessions));
    }

    @Bean
    AuthenticateAccessTokenUseCase authenticateAccessTokenUseCase(TokenProvider tokens, AccountPort accounts) {
        return new AccessTokenAuthenticationService(tokens, accounts);
    }
}
