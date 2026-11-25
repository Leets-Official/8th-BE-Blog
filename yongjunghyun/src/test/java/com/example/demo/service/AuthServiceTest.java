package com.example.demo.service;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.SignupRequest;
import com.example.demo.entity.Member;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.JwtProvider;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 단위 테스트: DB·서버 없이 AuthService 하나만 떼어 놓고, 주변(Repository 등)은 가짜(Mock)로 바꿔 검증한다
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @InjectMocks AuthService authService;

    private final SignupRequest signupRequest =
            new SignupRequest("new@leets.com", "password1234", "newbie", "용정현", "01012345678");

    @Test
    @DisplayName("회원가입 성공: 비밀번호는 원문이 아니라 해시값으로 저장된다")
    void signup_success_passwordIsHashed() {
        // given
        when(memberRepository.existsByEmail("new@leets.com")).thenReturn(false);
        when(memberRepository.existsByNickname("newbie")).thenReturn(false);
        when(passwordEncoder.encode("password1234")).thenReturn("HASHED");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        authService.signup(signupRequest);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertEquals("HASHED", captor.getValue().getPassword());
        assertNotEquals("password1234", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("회원가입 실패: 이미 있는 이메일이면 MEMBER4091, 저장하지 않는다")
    void signup_fail_duplicateEmail() {
        // given
        when(memberRepository.existsByEmail("new@leets.com")).thenReturn(true);

        // when
        CustomException e = assertThrows(CustomException.class, () -> authService.signup(signupRequest));

        // then
        assertEquals(ErrorCode.MEMBER_EMAIL_DUPLICATE, e.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패: 이미 있는 닉네임이면 MEMBER4092, 저장하지 않는다")
    void signup_fail_duplicateNickname() {
        // given
        when(memberRepository.existsByEmail("new@leets.com")).thenReturn(false);
        when(memberRepository.existsByNickname("newbie")).thenReturn(true);

        // when
        CustomException e = assertThrows(CustomException.class, () -> authService.signup(signupRequest));

        // then
        assertEquals(ErrorCode.MEMBER_NICKNAME_DUPLICATE, e.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호가 틀리면 AUTH4011, 토큰을 발급하지 않는다")
    void login_fail_wrongPassword() {
        // given
        Member member = Member.builder()
                .email("new@leets.com").password("HASHED").nickname("newbie").name("용정현").tel("01012345678")
                .build();
        when(memberRepository.findByEmail("new@leets.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong1234", "HASHED")).thenReturn(false);

        // when
        CustomException e = assertThrows(CustomException.class,
                () -> authService.login(new LoginRequest("new@leets.com", "wrong1234")));

        // then
        assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
        verifyNoInteractions(jwtProvider);
    }

    @Test
    @DisplayName("로그인 실패: 없는 이메일도 비밀번호 오류와 똑같이 AUTH4011")
    void login_fail_unknownEmail() {
        // given
        when(memberRepository.findByEmail("nobody@leets.com")).thenReturn(Optional.empty());

        // when
        CustomException e = assertThrows(CustomException.class,
                () -> authService.login(new LoginRequest("nobody@leets.com", "password1234")));

        // then
        assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
    }
}
