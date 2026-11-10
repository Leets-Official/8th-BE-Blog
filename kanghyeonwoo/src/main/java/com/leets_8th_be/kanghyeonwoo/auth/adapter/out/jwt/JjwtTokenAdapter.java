package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.jwt;

import com.leets_8th_be.kanghyeonwoo.auth.AuthFailure;
import com.leets_8th_be.kanghyeonwoo.auth.JwtTokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.VerifiedToken;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import java.time.Duration;
import org.springframework.stereotype.Component;
// Temporary bridge keeps one signing key while legacy refresh endpoints are still active.

@Component
public class JjwtTokenAdapter implements TokenProvider {
    private final JwtTokens legacy;

    public JjwtTokenAdapter(JwtTokens legacy) {
        this.legacy = legacy;
    }

    @Override
    public String issue(long userId, TokenType type, String sessionId, Duration lifetime) {
        return legacy.issue(userId, type.claim(), sessionId, lifetime);
    }

    @Override
    public VerifiedToken verify(String token, TokenType type) {
        try {
            var claims = legacy.verify(token, type.claim());
            return new VerifiedToken(claims.userId(), claims.sessionId());
        } catch (AuthFailure failure) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
    }
}
