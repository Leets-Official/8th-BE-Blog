package org.example._thbe.domain.auth.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example._thbe.global.code.BaseCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthErrorCode implements BaseCode {

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,"AUTH_401_1","토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"AUTH_401_2","토큰이 만료되었습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,"AUTH_403_1","접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
