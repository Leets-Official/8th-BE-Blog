package org.example._thbe.global.code;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode implements BaseCode{
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST,"COMMON_400_1","올바르지 않은 입력값입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
