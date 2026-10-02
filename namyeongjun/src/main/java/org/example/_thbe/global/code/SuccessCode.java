package org.example._thbe.global.code;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode implements BaseCode{
    COMMON_OK(HttpStatus.OK,"COMMON_200","요청 응답 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
