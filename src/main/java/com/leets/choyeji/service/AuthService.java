package com.leets.choyeji.service;

import com.leets.choyeji.auth.JwtTokenService;
import com.leets.choyeji.dto.LoginRequest;
import com.leets.choyeji.dto.LoginResponse;
import com.leets.choyeji.dto.SignupRequest;
import com.leets.choyeji.dto.SignupResponse;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.loginFailed();
        }
        Member member = memberRepository.findFirstByEmailOrderByIdAsc(request.email())
                .orElseThrow(ApiException::loginFailed);
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw ApiException.loginFailed();
        }
        return new LoginResponse(jwtTokenService.issue(member.getId()), "Bearer",
                JwtTokenService.ACCESS_TOKEN_TTL_SECONDS);
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.passwordTooLong();
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw ApiException.duplicateEmail();
        }
        Member member = new Member(passwordEncoder.encode(request.password()), request.nickname(), request.email());
        try {
            return SignupResponse.from(memberRepository.saveAndFlush(member));
        } catch (DataIntegrityViolationException exception) {
            // 사전 중복 확인 이후 동시에 가입한 요청도 DB 제약조건으로 차단합니다.
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && violation.getConstraintName() != null
                        && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_member_email")) {
                    throw ApiException.duplicateEmail();
                }
            }
            throw exception;
        }
    }
}
