package com.leets_8th_be.kanghyeonwoo.auth.config;

import com.leets_8th_be.kanghyeonwoo.auth.adapter.transaction.TransactionalSignup;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SignupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthModuleConfiguration {
    @Bean
    SignupUseCase signupUseCase(AccountPort accounts, PasswordHasher passwords) {
        return new TransactionalSignup(new SignupService(accounts, passwords));
    }
}
