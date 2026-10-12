package com.example.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "요청값이 올바르지 않습니다."),
    TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "TITLE_REQUIRED", "제목은 필수입니다."),
    TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "TITLE_TOO_LONG", "제목은 50자를 초과할 수 없습니다."),
    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_FORMAT", "요청 형식이 올바르지 않습니다."),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST, "INVALID_POST_ID", "게시글 ID가 올바르지 않습니다."),
    MISSING_USER_ID(HttpStatus.BAD_REQUEST, "MISSING_USER_ID", "X-User-Id 헤더는 필수입니다."),

    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST_ACCESS_DENIED", "게시글에 접근할 권한이 없습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "존재하지 않는 회원입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "존재하지 않는 게시글입니다."),
    POST_DELETED(HttpStatus.NOT_FOUND, "POST_DELETED", "삭제된 게시글입니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatusValue() {
        return status.value();
    }

}
