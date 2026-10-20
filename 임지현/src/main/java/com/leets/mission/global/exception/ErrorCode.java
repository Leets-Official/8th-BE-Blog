package com.leets.mission.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 Bad Request
    INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "페이지 번호가 올바르지 않습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 1~50 사이여야 합니다."),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 기준입니다."),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST, "게시글 ID 형식이 올바르지 않습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다."),
    TITLE_LENGTH_EXCEED(HttpStatus.BAD_REQUEST, "제목은 공백 포함 100자 이하여야 합니다."),
    CONTENT_LENGTH_EXCEED(HttpStatus.BAD_REQUEST, "본문은 공백 포함 100,000자 이하여야 합니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 403 Forbidden
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인이 작성한 게시글만 수정 및 삭제할 수 있습니다."),

    // 404 Not Found
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_POST_SUBMISSION(HttpStatus.CONFLICT, "잠시 후 다시 시도해 주세요. (중복 작성 방지)"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}