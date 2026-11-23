package com.leets_8th_be.kanghyeonwoo.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leets_8th_be.kanghyeonwoo.auth.adapter.out.crypto.BCryptPasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticationAccount;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.LoginService;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.SignupService;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    private final AccountPort accounts = mock(AccountPort.class);
    private final PasswordHasher passwords = new BCryptPasswordHasher(new BCryptPasswordEncoder());
    private final SignupUseCase signup = new SignupService(accounts, passwords);
    private final LoginUseCase login = new LoginService(accounts, passwords, mock(IssueSessionUseCase.class));

    @Test
    void signupNormalizesIdentityAndStoresSaltedHash() {
        when(accounts.register(anyString(), anyString(), anyString(), isNull()))
                .thenReturn(new AuthenticationAccount(7, "", null, "USER"));
        assertThat(signup.signup(" A@EXAMPLE.COM ", "password123", " Nick ")).isEqualTo(7);
        var hash = ArgumentCaptor.forClass(String.class);
        verify(accounts).register(eq("a@example.com"), eq("Nick"), hash.capture(), isNull());
        assertThat(hash.getValue()).isNotEqualTo("password123");
        assertThat(passwords.matches("password123", hash.getValue())).isTrue();
        assertThat(passwords.encode("password123")).isNotEqualTo(hash.getValue());
    }

    @Test
    void unknownAccountWrongPasswordAndLegacyProfileHaveSameFailure() {
        when(accounts.byEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(accounts.byEmail("known@example.com"))
                .thenReturn(Optional.of(new AuthenticationAccount(1, passwords.encode("password123"), null, "USER")));
        when(accounts.byEmail("legacy@example.com"))
                .thenReturn(Optional.of(new AuthenticationAccount(2, null, null, "USER")));
        for (String email : List.of("unknown@example.com", "known@example.com", "legacy@example.com")) {
            assertThatThrownBy(() -> login.login(email, "wrong-password"))
                    .isInstanceOf(AuthenticationFailure.class)
                    .hasMessage("인증에 실패했습니다.");
        }
    }
}
