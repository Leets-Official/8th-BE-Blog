package com.example.demo.service;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.ReissueRequest;
import com.example.demo.dto.auth.SignupRequest;
import com.example.demo.dto.auth.TokenResponse;
import com.example.demo.dto.member.MemberResponse;
import com.example.demo.entity.Member;
import com.example.demo.entity.RefreshToken;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String GRANT_TYPE = "Bearer";

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = memberRepository.save(
                Member.signUp(request.email(), request.nickname(), passwordEncoder.encode(request.password())));
        return new MemberResponse(member.getId(), member.getEmail(), member.getNickname());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
        return issueTokens(member.getId());
    }

    @Transactional(noRollbackFor = CustomException.class)
    public TokenResponse reissue(ReissueRequest request) {
        Long memberId = jwtTokenProvider.getMemberIdFromRefreshToken(request.refreshToken());
        RefreshToken saved = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!saved.getToken().equals(request.refreshToken())) {
            refreshTokenRepository.delete(saved);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return issueTokens(memberId);
    }

    private TokenResponse issueTokens(Long memberId) {
        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        refreshTokenRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        saved -> saved.rotate(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(memberId, refreshToken)));

        return new TokenResponse(GRANT_TYPE, accessToken, refreshToken);
    }
}