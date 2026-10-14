package com.example.demo.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400_1", "올바르지 않은 입력값입니다."),
    NO_FIELD_TO_UPDATE(HttpStatus.BAD_REQUEST, "COMMON_400_2", "수정할 제목 또는 내용을 입력해주세요."),
    FORBIDDEN_POST(HttpStatus.FORBIDDEN, "COMMON_403_1", "게시글에 대한 권한이 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404_1", "존재하지 않는 게시글입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "존재하지 않는 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}