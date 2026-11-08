package org.example._thbe.domain.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.auth.dto.AuthLoginRequest;
import org.example._thbe.domain.auth.dto.AuthLoginResponse;
import org.example._thbe.domain.auth.dto.AuthRefreshResponse;
import org.example._thbe.domain.auth.error.AuthErrorCode;
import org.example._thbe.domain.member.dto.MemberCreateRequest;
import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.domain.member.enums.MemberRole;
import org.example._thbe.domain.member.error.MemberErrorCode;
import org.example._thbe.domain.member.repository.MemberRepository;
import org.example._thbe.global.exception.CustomException;
import org.example._thbe.global.jwt.JwtUtil;
import org.example._thbe.global.redis.RedisRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisRepository redisRepository;
    private static final String REFRESH_PREFIX = "refresh:";

    @Transactional
    public void createMember(MemberCreateRequest request) {

        if(memberRepository.existsByEmail(request.email())) {
            throw new CustomException(MemberErrorCode.EMAIL_ALREADY_EXIST);
        }


        Member member = Member.create(
                request.name(),
                passwordEncoder.encode(request.password()),
                request.email(),
                MemberRole.MEMBER
        );

        memberRepository.save(member);
    }

    public AuthLoginResponse loginMember(AuthLoginRequest request) {
        Member member = validateLogin(request.email(), request.password());
        String id = member.getId().toString();

        String accessToken = jwtUtil.createAccessToken(id, member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(id, member.getRole());

        redisRepository.save(REFRESH_PREFIX + id, refreshToken, jwtUtil.getRefreshExp());

        return new AuthLoginResponse(accessToken, jwtUtil.getExp(accessToken), refreshToken, jwtUtil.getExp(refreshToken));
    }


    public AuthRefreshResponse refreshToken(String refreshToken) {
        Claims claims = jwtUtil.getClaims(refreshToken);
        if (claims == null || !"refresh".equals(claims.get("type", String.class))) {
            throw new CustomException(AuthErrorCode.TOKEN_INVALID);
        }

        String memberId = claims.getSubject();
        if (!isTokenValid(REFRESH_PREFIX + memberId, refreshToken)) {
            throw new CustomException(AuthErrorCode.TOKEN_INVALID);
        }

        MemberRole role = MemberRole.valueOf(claims.get("role", String.class));
        return new AuthRefreshResponse(jwtUtil.createAccessToken(memberId, role));
    }

    private Member validateLogin(String email,String password) {
        Member member = memberRepository
                .findByEmail(email).orElseThrow(()-> new CustomException(MemberErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(MemberErrorCode.LOGIN_FAILED);
        }

        return member;
    }

    private boolean isTokenValid(String key, String refreshToken) {
        return redisRepository.find(key)
                .map(saved -> saved.equals(refreshToken))
                .orElse(false);
    }


}
