package com.leets.blog.domain.auth.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.security.jwt.JwtProperties;
import com.leets.blog.common.security.jwt.JwtProvider;
import com.leets.blog.domain.auth.entity.RefreshToken;
import com.leets.blog.domain.auth.repository.RefreshTokenRepository;
import com.leets.blog.domain.auth.web.dto.request.LoginRequest;
import com.leets.blog.domain.auth.web.dto.request.ReissueRequest;
import com.leets.blog.domain.auth.web.dto.request.SignUpRequest;
import com.leets.blog.domain.auth.web.dto.response.SignUpResponse;
import com.leets.blog.domain.auth.web.dto.response.TokenResponse;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // BCrypt는 72바이트까지만 처리 (넘으면 encode()가 예외 → 500)
    private static final int BCRYPT_MAX_BYTES = 72;

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    // 회원가입: 비밀번호 바이트(400) → 이메일 중복(409) → 닉네임 중복(409) → BCrypt 해시 후 저장
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (exceedsBcryptLimit(request.password())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        // 탈퇴 회원의 이메일·닉네임도 unique 제약 때문에 재사용 불가 → 중복으로 처리
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = Member.createLocal(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname());
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // 동시에 가입해 중복 확인을 함께 통과한 경우: 걸린 unique 제약 이름으로 구분
            throw new CustomException(duplicateErrorOf(e));
        }
        return SignUpResponse.from(member);
    }

    // 로그인: 이메일이 없든 비밀번호가 틀리든 같은 401
    @Transactional
    public TokenResponse login(LoginRequest request) {
        if (exceedsBcryptLimit(request.password())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(request.email())
                .filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
        return issueTokens(member);
    }

    // 재발급: 서명·만료 확인 → DB의 최신 토큰과 비교 → 새 Access·Refresh 발급 (rotation)
    // noRollbackFor: 재사용 감지 시 토큰 삭제 후 예외를 던지는데, 롤백되면 삭제도 취소되므로 커밋되게 함
    @Transactional(noRollbackFor = CustomException.class)
    public TokenResponse reissue(ReissueRequest request) {
        Long memberId = jwtProvider.parseRefreshToken(request.refreshToken());
        RefreshToken saved = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 이미 교체된 옛 토큰이 다시 쓰임 → 최신 토큰도 폐기해 다시 로그인하도록
        if (!saved.matches(request.refreshToken())) {
            refreshTokenRepository.delete(saved);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // role이 바뀌었을 수 있으므로 회원을 다시 읽어 최신 role로 발급
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
        return issueTokens(member);
    }

    // 로그인·재발급 공통 (카카오 로그인에서도 사용 예정): 토큰 발급 + Refresh Token 저장(있으면 교체)
    @Transactional
    public TokenResponse issueTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        saved -> saved.rotate(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.of(member.getId(), refreshToken)));

        return TokenResponse.of(accessToken, refreshToken, jwtProperties.accessTokenValidity().toSeconds());
    }

    private boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES;
    }

    private ErrorCode duplicateErrorOf(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        if (message != null && message.toLowerCase().contains(Member.UK_NICKNAME)) {
            return ErrorCode.DUPLICATE_NICKNAME;
        }
        return ErrorCode.DUPLICATE_EMAIL;
    }
}
