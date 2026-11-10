package com.leets_8th_be.kanghyeonwoo.auth.domain.exception;

public enum AuthenticationError {
    INVALID_INPUT("입력값이 올바르지 않습니다."),
    INVALID_CREDENTIALS("인증에 실패했습니다."),
    ACCOUNT_CONFLICT("이미 가입된 계정이 있습니다. 기존 로그인 수단을 이용해 주세요."),
    PROVIDER_UNAVAILABLE("카카오 로그인 설정 또는 연결을 확인해 주세요.");
    private final String message;

    AuthenticationError(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
