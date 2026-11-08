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
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 요청 형식입니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "유효한 인증 정보가 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 재발급 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "요청을 수행할 권한이 없습니다."),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "DUPLICATE_MEMBER", "이미 사용 중인 이메일 또는 닉네임입니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "KAKAO_LOGIN_FAILED", "카카오 로그인에 실패했습니다."),
    KAKAO_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "KAKAO_PROVIDER_ERROR", "카카오 로그인 서버의 응답을 처리할 수 없습니다."),
    KAKAO_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "KAKAO_NOT_CONFIGURED", "카카오 로그인 설정이 필요합니다."),

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
