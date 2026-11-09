package com.leets_8th_be.kanghyeonwoo.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.*;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    private final Accounts accounts = mock(Accounts.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuthenticationService auth = new AuthenticationService(
            accounts,
            new Passwords() {
                public String encode(String raw) {
                    return encoder.encode(raw);
                }

                public boolean matches(String raw, String hash) {
                    return encoder.matches(raw, hash);
                }
            },
            new JwtTokens(""),
            mock(Sessions.class));

    @Test
    void signupNormalizesIdentityAndStoresSaltedHash() {
        when(accounts.register(anyString(), anyString(), anyString(), isNull()))
                .thenReturn(new Account(7, "", null, "USER"));
        assertThat(auth.signup(" A@EXAMPLE.COM ", "password123", " Nick ")).isEqualTo(7);
        var hash = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(accounts).register(eq("a@example.com"), eq("Nick"), hash.capture(), isNull());
        assertThat(hash.getValue()).isNotEqualTo("password123");
        assertThat(encoder.matches("password123", hash.getValue())).isTrue();
        assertThat(encoder.encode("password123")).isNotEqualTo(hash.getValue());
    }

    @Test
    void unknownAccountWrongPasswordAndLegacyProfileHaveSameFailure() {
        when(accounts.byEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(accounts.byEmail("known@example.com"))
                .thenReturn(Optional.of(new Account(1, encoder.encode("password123"), null, "USER")));
        when(accounts.byEmail("legacy@example.com")).thenReturn(Optional.of(new Account(2, null, null, "USER")));
        for (String email : java.util.List.of("unknown@example.com", "known@example.com", "legacy@example.com"))
            assertThatThrownBy(() -> auth.login(email, "wrong-password"))
                    .isInstanceOf(AuthFailure.class)
                    .hasMessage("인증에 실패했습니다.");
    }
}
