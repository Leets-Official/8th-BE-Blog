package com.leets.blog.service;

import com.leets.blog.dto.auth.*;
import com.leets.blog.entity.RefreshToken;
import com.leets.blog.entity.User;
import com.leets.blog.exception.PostApiException;
import com.leets.blog.repository.*;
import com.leets.blog.security.TokenService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("mysql")
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;

    public SignupResponse signup(SignupRequest request) {
        if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new PostApiException(400, "INVALID_INPUT", "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        String email = normalizeEmail(request.email());
        String nickname = request.nickname().strip();
        if (users.existsByEmail(email)) throw new PostApiException(409, "DUPLICATE_EMAIL", "이미 가입된 이메일입니다.");
        if (users.existsByNickname(nickname)) throw new PostApiException(409, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        return SignupResponse.from(users.saveAndFlush(new User(email, passwordEncoder.encode(request.password()), nickname)));
    }

    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        } catch (AuthenticationException e) {
            throw new PostApiException(401, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        User user = users.findByEmailAndDeletedAtIsNull(email).orElseThrow(AuthService::invalidRefresh);
        users.findActiveForUpdate(user.getId()).orElseThrow(AuthService::invalidRefresh);
        return issue(user.getId());
    }

    public TokenResponse refresh(RefreshRequest request) {
        Long userId = tokens.refreshUserId(request.refreshToken());
        users.findActiveForUpdate(userId).orElseThrow(AuthService::invalidRefresh);
        var stored = refreshTokens.findByUserId(userId).orElseThrow(AuthService::invalidRefresh);
        if (!stored.getTokenHash().equals(tokens.hash(request.refreshToken()))) throw invalidRefresh();
        return issue(userId);
    }

    private TokenResponse issue(Long userId) {
        TokenResponse response = tokens.issue(userId);
        refreshTokens.save(new RefreshToken(userId, tokens.hash(response.refreshToken())));
        return response;
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static PostApiException invalidRefresh() {
        return new PostApiException(401, "INVALID_REFRESH_TOKEN", "유효하지 않거나 만료된 Refresh Token입니다.");
    }
}
