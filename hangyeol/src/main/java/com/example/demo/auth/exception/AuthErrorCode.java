package com.example.demo.auth.exception;

import com.example.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,
            "AUTH409_1",
            "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,
            "AUTH409_2",
            "이미 사용 중인 닉네임입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
            "AUTH401_2",
            "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_3",
            "유효하지 않은 리프레시 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}