package com.leets_8th_be.kanghyeonwoo.auth.domain.exception;

public final class RefreshTokenReused extends AuthenticationFailure {
    public RefreshTokenReused() {
        super(AuthenticationError.INVALID_CREDENTIALS);
    }
}
