package com.leets_8th_be.kanghyeonwoo.auth.application.port.out;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.VerifiedToken;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;
import java.time.Duration;

public interface TokenProvider {
    String issue(long userId, TokenType type, String sessionId, Duration lifetime);

    VerifiedToken verify(String token, TokenType type);
}
