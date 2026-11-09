package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.SignupResponse;
import com.example.demo.dto.TokenResponse;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.global.CustomException;
import com.example.demo.global.ErrorCode;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // BCrypt 는 72바이트를 넘는 부분을 무시한다. (한글은 글자당 3바이트라 50자도 넘길 수 있음)
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new CustomException(ErrorCode.PASSWORD_TOO_LONG);
        }

        String email = normalize(request.email());

        // 1차 방어: 사전 체크로 친절한 에러 반환
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 2차 방어: 동시 가입이 사전 체크를 같이 통과해도 DB 유니크 제약이 막는다.
        // (어느 제약이 걸렸는지 구분하려면 제약 이름을 파싱해야 해서 이메일 중복으로 통일)
        try {
            User user = userRepository.saveAndFlush(User.builder()
                    .email(email)
                    .nickname(request.nickname())
                    .password(passwordEncoder.encode(request.password())) // 원문 저장 금지
                    .build());
            return SignupResponse.from(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 응답 (계정 존재 여부 노출 방지)
        User user = userRepository.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
        return issueTokens(user.getId());
    }

    // 재사용 감지 시 Refresh Token 을 폐기(delete)한 뒤 예외를 던진다.
    // 기본 설정이면 예외 때문에 폐기까지 롤백되므로 noRollbackFor 가 꼭 필요하다.
    @Transactional(noRollbackFor = CustomException.class)
    public TokenResponse reissue(String refreshToken) {
        Long userId;
        try {
            userId = jwtProvider.parse(refreshToken, JwtProvider.REFRESH);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 저장된 해시와 다르면 이미 회전되어 폐기된 토큰을 다시 쓴 것 -> 탈취 가능성
        // 연결된 재발급 권한을 폐기해서 (정상 사용자 포함) 다시 로그인하게 만든다.
        if (!stored.matches(sha256(refreshToken))) {
            refreshTokenRepository.delete(stored);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return issueTokens(userId);
    }

    // Access + Refresh 를 새로 발급하고, 저장된 Refresh 해시를 교체(회전)한다.
    private TokenResponse issueTokens(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        String hash = sha256(refreshToken);

        refreshTokenRepository.findByUserId(userId).ifPresentOrElse(
                stored -> stored.rotate(hash),
                () -> refreshTokenRepository.save(new RefreshToken(userId, hash)));

        return new TokenResponse(accessToken, refreshToken);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
