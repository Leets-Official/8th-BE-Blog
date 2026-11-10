package com.leets_8th_be.kanghyeonwoo.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticationAccount;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.IssueSessionUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.LoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.PasswordHasher;
import com.leets_8th_be.kanghyeonwoo.auth.application.usecase.LoginService;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LoginUseCaseTest {
    private final AccountPort accounts = mock(AccountPort.class);
    private final PasswordHasher passwords = mock(PasswordHasher.class);
    private final IssueSessionUseCase sessions = mock(IssueSessionUseCase.class);

    @Test
    void verifiedPasswordStartsLoginSession() {
        when(accounts.byEmail("user@example.com"))
                .thenReturn(Optional.of(new AuthenticationAccount(7, "stored-hash", null, "USER")));
        when(passwords.matches("correct-password", "stored-hash")).thenReturn(true);
        Tokens tokens = new Tokens("access", "refresh", 600);
        when(sessions.start(7)).thenReturn(tokens);
        LoginUseCase login = new LoginService(accounts, passwords, sessions);

        assertThat(login.login(" USER@EXAMPLE.COM ", "correct-password")).isEqualTo(tokens);
        verify(sessions).start(7);
    }

    @Test
    void missingWrongPasswordAndLegacyAccountsHaveSameFailure() {
        when(passwords.encode(anyString())).thenReturn("dummy-hash");
        when(accounts.byEmail("known@example.com"))
                .thenReturn(Optional.of(new AuthenticationAccount(1, "stored-hash", null, "USER")));
        when(accounts.byEmail("legacy@example.com"))
                .thenReturn(Optional.of(new AuthenticationAccount(2, null, null, "USER")));
        LoginUseCase login = new LoginService(accounts, passwords, sessions);
        for (String email : java.util.List.of("unknown@example.com", "known@example.com", "legacy@example.com")) {
            assertThatThrownBy(() -> login.login(email, "wrong-password"))
                    .isInstanceOf(AuthenticationFailure.class)
                    .hasMessage("인증에 실패했습니다.");
        }
        verifyNoInteractions(sessions);
    }
}
