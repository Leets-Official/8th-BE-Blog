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

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "존재하지 않거나 삭제된 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT_ACCESS_DENIED", "댓글에 접근할 권한이 없습니다."),
    SELF_REPORT_DENIED(HttpStatus.FORBIDDEN, "SELF_REPORT_DENIED", "본인의 게시글이나 댓글은 신고할 수 없습니다."),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "DUPLICATE_REPORT", "이미 신고한 대상입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "존재하지 않는 신고입니다."),
    REPORT_STATE_CONFLICT(HttpStatus.CONFLICT, "REPORT_STATE_CONFLICT", "현재 상태에서는 신고를 승인할 수 없습니다."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION", "다른 요청으로 데이터가 변경되었습니다. 최신 상태를 확인한 후 다시 요청해주세요."),
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
