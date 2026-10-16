package com.example.demo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "요청 형식이 올바르지 않습니다."),
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "COMMON400", "페이지 번호가 올바르지 않습니다."),

    // 게시글
    POST_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "POST4001", "제목은 필수입니다."),
    POST_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "POST4002", "제목은 100자 이하로 입력해주세요."),
    POST_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "POST4003", "본문은 필수입니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST4031", "본인이 작성한 게시글만 수정/삭제할 수 있습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4041", "게시글을 찾을 수 없습니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4041", "회원을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}