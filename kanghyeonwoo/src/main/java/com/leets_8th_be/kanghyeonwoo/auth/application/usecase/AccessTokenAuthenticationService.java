package com.leets_8th_be.kanghyeonwoo.auth.application.usecase;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticatedUser;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.AuthenticateAccessTokenUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.AccountPort;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.TokenProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.TokenType;

public class AccessTokenAuthenticationService implements AuthenticateAccessTokenUseCase {
    private final TokenProvider tokens;
    private final AccountPort accounts;

    public AccessTokenAuthenticationService(TokenProvider tokens, AccountPort accounts) {
        this.tokens = tokens;
        this.accounts = accounts;
    }

    @Override
    public AuthenticatedUser authenticate(String token) {
        var verified = tokens.verify(token, TokenType.ACCESS);
        var account = accounts.byId(verified.userId())
                .orElseThrow(() -> new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS));
        return new AuthenticatedUser(account.userId(), account.role());
    }
}
