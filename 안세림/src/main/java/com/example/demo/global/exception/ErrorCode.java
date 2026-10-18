package com.example.demo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4001", "해당 ID의 게시글을 찾을 수 없습니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST4003", "작성자 본인만 수정 및 삭제할 수 있습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "해당 ID의 회원을 찾을 수 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON400_VALIDATION", "입력값이 올바르지 않습니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "COMMON400_TYPE", "요청 데이터의 형식이 올바르지 않습니다."),
    EMPTY_BODY(HttpStatus.BAD_REQUEST, "COMMON400_BODY", "전송된 요청 본문 내용이 없습니다."),
    NOT_FOUND_PATH(HttpStatus.NOT_FOUND, "COMMON404", "요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "지원하지 않는 요청 방식입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}