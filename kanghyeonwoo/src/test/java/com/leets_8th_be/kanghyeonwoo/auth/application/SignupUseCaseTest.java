package com.leets_8th_be.kanghyeonwoo.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticationAccount;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SignupService;
import org.junit.jupiter.api.Test;

class SignupUseCaseTest {
    @Test
    void registersNormalizedIdentityWithPasswordHash() {
        AccountPort accounts = mock(AccountPort.class);
        PasswordHasher passwords = mock(PasswordHasher.class);
        when(passwords.encode("password123")).thenReturn("salted-hash");
        when(accounts.register(anyString(), anyString(), anyString(), isNull()))
                .thenReturn(new AuthenticationAccount(7, "salted-hash", null, "USER"));
        SignupUseCase signup = new SignupService(accounts, passwords);

        assertThat(signup.signup(" USER@EXAMPLE.COM ", "password123", " Nick ")).isEqualTo(7);
        verify(accounts).register("user@example.com", "Nick", "salted-hash", null);
    }
}
