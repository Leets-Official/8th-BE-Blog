package com.example.demo.service;

import com.example.demo.dto.auth.*;
import com.example.demo.entity.Member;
import com.example.demo.entity.RefreshToken;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.JwtProvider;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입: 중복 검사 → 비밀번호 해시 → 저장
    // (검사와 저장 사이에 같은 값이 들어오는 경우는 DB unique 제약이 막고, GlobalExceptionHandler가 409로 응답한다)
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.MEMBER_EMAIL_DUPLICATE);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_NICKNAME_DUPLICATE);
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))   // 원문은 저장하지 않는다
                .nickname(request.nickname())
                .name(request.name())
                .tel(request.tel())
                .build();

        return SignupResponse.from(memberRepository.save(member));
    }

    // 로그인: 이메일로 회원 찾기 → 비밀번호 비교 → 토큰 2개 발급
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 응답한다
        // (다르게 응답하면 "이 이메일은 가입되어 있다"는 정보가 새어 나간다)
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(member.getId());
    }

    // 재발급: 리프레시 토큰 검증 → 저장된 값과 비교 → 새 토큰 2개 발급(회전)
    @Transactional(noRollbackFor = CustomException.class)  // 재사용 감지 시 삭제한 것은 예외가 나도 유지되어야 한다
    public TokenResponse reissue(ReissueRequest request) {
        Long memberId = jwtProvider.getMemberIdFromRefreshToken(request.refreshToken());

        RefreshToken saved = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!saved.getToken().equals(request.refreshToken())) {
            // 이미 한 번 쓰고 교체된 토큰이 또 왔다 = 누군가 훔쳐 쓴 것일 수 있다 → 저장된 토큰도 폐기해 강제 재로그인
            refreshTokenRepository.delete(saved);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        saved.rotate(refreshToken, jwtProvider.getRefreshTokenExpiresAt());   // 변경 감지로 UPDATE

        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getAccessTokenValiditySeconds());
    }

    // 로그아웃: 저장된 리프레시 토큰을 지워 더 이상 재발급되지 않게 한다
    // (이미 발급된 액세스 토큰은 만료(10분)될 때까지 유효하다 — 그래서 짧게 잡는다)
    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private TokenResponse issueTokens(Long memberId) {
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);

        // 회원당 리프레시 토큰은 하나: 있으면 교체, 없으면 새로 저장
        refreshTokenRepository.findByMemberId(memberId).ifPresentOrElse(
                saved -> saved.rotate(refreshToken, jwtProvider.getRefreshTokenExpiresAt()),
                () -> refreshTokenRepository.save(
                        new RefreshToken(memberId, refreshToken, jwtProvider.getRefreshTokenExpiresAt())));

        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getAccessTokenValiditySeconds());
    }
}
