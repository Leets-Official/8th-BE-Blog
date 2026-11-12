package com.leets.mission.service;

import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.dto.auth.LoginRequest;
import com.leets.mission.dto.auth.RefreshRequest;
import com.leets.mission.dto.auth.SignupRequest;
import com.leets.mission.dto.auth.TokenResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public void signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        memberRepository.save(member);
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return tokenService.issue(member);
    }

    public TokenResponse refresh(RefreshRequest request) {
        return tokenService.refresh(request.refreshToken());
    }
}
