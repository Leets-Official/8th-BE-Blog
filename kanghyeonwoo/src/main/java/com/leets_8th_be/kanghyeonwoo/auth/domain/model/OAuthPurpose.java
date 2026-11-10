package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

public enum OAuthPurpose {
    STATE("state"),
    EXCHANGE("exchange"),
    SIGNUP("signup");
    private final String value;

    OAuthPurpose(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OAuthPurpose fromValue(String value) {
        for (OAuthPurpose purpose : values()) {
            if (purpose.value.equals(value)) {
                return purpose;
            }
        }
        throw new IllegalArgumentException("알 수 없는 OAuth 티켓 종류입니다.");
    }
}
