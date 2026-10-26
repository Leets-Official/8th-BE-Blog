package com.leets_8th_be.kanghyeonwoo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SEARCH_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "SEARCH4001", "해당 검색어를 찾을 수 없습니다."),

    // 게시판 리소스
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4001", "해당 ID의 게시글을 찾을 수 없습니다."),
    FORBIDDEN_POST_ACCESS(HttpStatus.FORBIDDEN, "POST4003", "작성자 본인만 수정 및 삭제할 수 있습니다."),

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4001", "해당 신고를 찾을 수 없습니다."),
    REPORT_ALREADY_RESOLVED(HttpStatus.CONFLICT, "REPORT4093", "이미 처리된 신고입니다."),
    REPORT_TARGET_DELETED(HttpStatus.CONFLICT, "REPORT4094", "삭제된 대상은 숨길 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT4001", "해당 댓글을 찾을 수 없습니다."),
    DELETED_COMMENT_REPORT(HttpStatus.CONFLICT, "REPORT4092", "삭제된 댓글은 신고할 수 없습니다."),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "REPORT4091", "이미 신고한 대상입니다."),
    SELF_REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "REPORT4031", "본인 콘텐츠는 신고할 수 없습니다."),

    // 사용자 리소스
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4001", "해당 ID의 사용자를 찾을 수 없습니다."),

    // 공통 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON400_VALIDATION", "입력값이 올바르지 않습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON400_TYPE", "요청 데이터의 형식이 올바르지 않습니다."),
    MISSING_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON400_BODY", "전송된 요청 본문 내용이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "지원하지 않는 요청 방식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
