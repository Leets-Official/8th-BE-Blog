package com.example.demo.service;

import com.example.demo.controller.ApiExceptionHandler.ApiException;
import com.example.demo.domain.member.Member;
import com.example.demo.domain.member.MemberRepository;
import com.example.demo.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 사전 중복 검사로 원인을 안내하고 DB 제약으로 동시 가입도 차단합니다.
    public AuthDto.MemberView signup(AuthDto.Signup request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String nickname = request.nickname().trim();
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(400, "INVALID_PASSWORD", "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new ApiException(409, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByName(nickname)) {
            throw new ApiException(409, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        }
        try {
            Member member = memberRepository.saveAndFlush(new Member(nickname, null, email,
                    passwordEncoder.encode(request.password())));
            return new AuthDto.MemberView(member.getId(), member.getEmail(), member.getName());
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(409, "DUPLICATE_MEMBER", "이미 사용 중인 이메일 또는 닉네임입니다.");
        }
    }

    // 비밀번호 원문을 저장하지 않고 BCrypt 해시와 비교합니다.
    public AuthDto.Tokens login(AuthDto.Login request) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(request.email().trim().toLowerCase(Locale.ROOT))
                .filter(m -> request.password().getBytes(StandardCharsets.UTF_8).length <= 72
                        && passwordEncoder.matches(request.password(), m.getPassword()))
                .orElseThrow(() -> new ApiException(401, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
        return issueTokens(member);
    }

    // 저장된 해시와 만료 시각을 확인하고 사용한 Refresh Token을 교체합니다.
    public AuthDto.Tokens refresh(AuthDto.Refresh request) {
        Member member = memberRepository.findByRefreshTokenHashAndDeletedAtIsNull(hash(request.refreshToken()))
                .filter(m -> m.getRefreshExpiresAt() != null
                        && m.getRefreshExpiresAt().isAfter(LocalDateTime.now(ZoneOffset.UTC)))
                .orElseThrow(() -> new ApiException(401, "INVALID_REFRESH_TOKEN", "유효하지 않거나 만료된 Refresh Token입니다."));
        return issueTokens(member);
    }

    // Access Token은 15분, 일회용 Refresh Token은 7일 동안 유효합니다.
    private AuthDto.Tokens issueTokens(Member member) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("blog").subject(member.getId().toString())
                .id(UUID.randomUUID().toString()).issuedAt(now).expiresAt(now.plusSeconds(900)).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        member.updateRefreshToken(hash(refreshToken), LocalDateTime.ofInstant(now.plusSeconds(604800), ZoneOffset.UTC));
        return new AuthDto.Tokens("Bearer", accessToken, refreshToken, 900);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
