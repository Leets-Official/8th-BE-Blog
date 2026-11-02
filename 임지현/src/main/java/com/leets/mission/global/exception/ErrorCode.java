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
    INVALID_REPORT_TARGET(HttpStatus.BAD_REQUEST, "신고 대상은 게시글 또는 댓글 중 하나만 지정해야 합니다."),
    ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "운영자만 수행할 수 있는 요청입니다."),

    // 404 Not Found
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 신고를 찾을 수 없습니다."),


    // 409 Conflict
    DUPLICATE_POST_SUBMISSION(HttpStatus.CONFLICT, "잠시 후 다시 시도해 주세요.(게시글 중복 작성 방지)"),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "이미 신고한 대상입니다."),
    REPORT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 신고입니다."),
    /**
     * 관리자가 이미 숨김 처리된 게시글을 중복해서 숨기려 할 때 발생 (일반 조회용 아님)
     */
    POST_ALREADY_HIDDEN(HttpStatus.CONFLICT, "이미 숨김 처리된 게시글입니다."),
    COMMENT_ALREADY_HIDDEN(HttpStatus.CONFLICT, "이미 숨김 처리된 댓글입니다."),
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인이 작성한 콘텐츠는 신고할 수 없습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}