package com.example.demo.auth.service;

import com.example.demo.auth.dto.AuthReqDto;
import com.example.demo.auth.dto.AuthResDto;
import com.example.demo.auth.entity.RefreshToken;
import com.example.demo.auth.exception.AuthErrorCode;
import com.example.demo.auth.repository.RefreshTokenRepository;
import com.example.demo.global.apiPayload.exception.GeneralException;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;


    @Transactional
    public AuthResDto.SignupResponse signup(AuthReqDto.SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new GeneralException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByName(request.nickname())) {
            throw new GeneralException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.nickname())
                .build();

        Member savedMember = memberRepository.save(member);

        return new AuthResDto.SignupResponse(savedMember.getId());
    }

    @Transactional
    public AuthResDto.TokenResult login(AuthReqDto.LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new GeneralException(AuthErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new GeneralException(AuthErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        RefreshToken token = RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtProvider.getRefreshTokenValidity())))
                .build();
        refreshTokenRepository.save(token);

        return new AuthResDto.TokenResult(accessToken, refreshToken);
    }

    @Transactional
    public AuthResDto.TokenResult reissue(String refreshToken) {
        if (refreshToken == null || !jwtProvider.isValidRefreshToken(refreshToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        Long memberId = savedToken.getMember().getId();

        String newAccessToken = jwtProvider.createAccessToken(memberId);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId);

        savedToken.rotate(
                newRefreshToken,
                LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenValidity()))
        );

        return new AuthResDto.TokenResult(newAccessToken, newRefreshToken);
    }
}
