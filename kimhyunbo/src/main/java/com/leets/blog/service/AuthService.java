package com.leets.blog.service;

import com.leets.blog.dto.auth.LoginRequest;
import com.leets.blog.dto.auth.ReissueRequest;
import com.leets.blog.dto.auth.SignupRequest;
import com.leets.blog.dto.auth.SignupResponse;
import com.leets.blog.dto.auth.TokenResponse;
import com.leets.blog.entity.Member;
import com.leets.blog.entity.RefreshToken;
import com.leets.blog.global.exception.CustomException;
import com.leets.blog.global.exception.ErrorCode;
import com.leets.blog.global.security.JwtProvider;
import com.leets.blog.repository.MemberRepository;
import com.leets.blog.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String GRANT_TYPE = "Bearer";

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateNotDuplicated(request.email(), request.nickname());

        Member member = Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(passwordEncoder.encode(request.password()))
                .build();

        // 같은 이메일/닉네임으로 동시에 가입해 위 검사를 함께 통과하면, 유니크 제약 위반으로 한 번 더 막는다.
        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(memberRepository.existsByEmail(request.email())
                    ? ErrorCode.EMAIL_DUPLICATED
                    : ErrorCode.NICKNAME_DUPLICATED);
        }

        return SignupResponse.from(member);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일이 없는지, 비밀번호가 틀렸는지 구분하지 않고 같은 에러를 준다. (가입된 이메일을 알아낼 수 없게)
        Member member = memberRepository.findByEmail(request.email())
                .filter(found -> !found.isDeleted())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        return issueTokens(member.getId());
    }

    @Transactional
    public TokenResponse reissue(ReissueRequest request) {
        Long memberId = jwtProvider.getMemberIdFromRefreshToken(request.refreshToken());

        // 서명이 올바르더라도, 마지막으로 발급한 Refresh Token이 아니면 쓸 수 없다. (이미 재발급에 쓴 토큰 재사용 방지)
        RefreshToken saved = refreshTokenRepository.findByMemberId(memberId)
                .filter(token -> token.matches(request.refreshToken()))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        saved.rotate(refreshToken, refreshTokenExpiresAt());

        return toTokenResponse(accessToken, refreshToken);
    }

    private void validateNotDuplicated(String email, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
    }

    private TokenResponse issueTokens(Long memberId) {
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);

        // 다시 로그인하면 이전 Refresh Token은 새 토큰으로 바뀐다.
        refreshTokenRepository.findByMemberId(memberId).ifPresentOrElse(
                saved -> saved.rotate(refreshToken, refreshTokenExpiresAt()),
                () -> refreshTokenRepository.save(new RefreshToken(memberId, refreshToken, refreshTokenExpiresAt())));

        return toTokenResponse(accessToken, refreshToken);
    }

    private LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now().plus(jwtProvider.getRefreshTokenValidity());
    }

    private TokenResponse toTokenResponse(String accessToken, String refreshToken) {
        return new TokenResponse(GRANT_TYPE, accessToken, refreshToken,
                jwtProvider.getAccessTokenValidity().toSeconds());
    }
}
