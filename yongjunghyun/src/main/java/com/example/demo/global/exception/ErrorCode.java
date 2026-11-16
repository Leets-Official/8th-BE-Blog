package com.example.demo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "요청 형식이 올바르지 않습니다."),
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "COMMON4001", "페이지 번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "접근 권한이 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON409", "이미 존재하는 데이터입니다."),

    // 인증 (토큰·로그인)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4011", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4014", "유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_EMPTY(HttpStatus.BAD_REQUEST, "AUTH4001", "리프레시 토큰은 필수입니다."),

    // 게시글
    POST_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "POST4001", "제목은 필수입니다."),
    POST_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "POST4002", "제목은 100자 이하로 입력해주세요."),
    POST_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "POST4003", "본문은 필수입니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST4031", "본인이 작성한 게시글만 수정/삭제할 수 있습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4041", "게시글을 찾을 수 없습니다."),

    // 회원
    MEMBER_EMAIL_EMPTY(HttpStatus.BAD_REQUEST, "MEMBER4001", "이메일은 필수입니다."),
    MEMBER_EMAIL_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4002", "이메일 형식이 올바르지 않습니다."),
    MEMBER_PASSWORD_EMPTY(HttpStatus.BAD_REQUEST, "MEMBER4003", "비밀번호는 필수입니다."),
    MEMBER_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4004", "비밀번호는 영문과 숫자를 포함해 8~20자로 입력해주세요."),
    MEMBER_NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "MEMBER4005", "닉네임은 필수입니다."),
    MEMBER_NICKNAME_TOO_LONG(HttpStatus.BAD_REQUEST, "MEMBER4006", "닉네임은 20자 이하로 입력해주세요."),
    MEMBER_NAME_EMPTY(HttpStatus.BAD_REQUEST, "MEMBER4007", "이름은 필수입니다."),
    MEMBER_TEL_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4008", "전화번호는 01로 시작하는 숫자 10~11자리로 입력해주세요."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4041", "회원을 찾을 수 없습니다."),
    MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER4091", "이미 사용 중인 이메일입니다."),
    MEMBER_NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "MEMBER4092", "이미 사용 중인 닉네임입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
