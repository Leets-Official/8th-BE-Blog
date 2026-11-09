package com.leets_8th_be.kanghyeonwoo.shared.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiResponseCode {
    SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    RESOURCE_CREATED(HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 데이터의 형식이 올바르지 않습니다."),
    MISSING_REQUEST_BODY(HttpStatus.BAD_REQUEST, "전송된 요청 본문 내용이 없습니다."),

    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "작성자 본인만 수정 및 삭제할 수 있습니다."),
    SELF_REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 콘텐츠는 신고할 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 사용자를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 신고를 찾을 수 없습니다."),
    SEARCH_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 검색어를 찾을 수 없습니다."),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청 경로를 찾을 수 없습니다."),

    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 등록된 리소스입니다."),
    REPORT_ALREADY_RESOLVED(HttpStatus.CONFLICT, "이미 처리된 신고입니다."),
    RESOURCE_DELETED(HttpStatus.CONFLICT, "삭제된 리소스는 이 작업을 수행할 수 없습니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
