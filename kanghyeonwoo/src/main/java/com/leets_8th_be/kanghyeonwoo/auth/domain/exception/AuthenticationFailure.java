package com.leets_8th_be.kanghyeonwoo.auth.domain.exception;

public class AuthenticationFailure extends RuntimeException {
    private final AuthenticationError reason;

    public AuthenticationFailure(AuthenticationError reason) {
        this(reason, reason.message());
    }

    public AuthenticationFailure(AuthenticationError reason, String message) {
        super(message);
        this.reason = reason;
    }

    public AuthenticationError reason() {
        return reason;
    }
}
