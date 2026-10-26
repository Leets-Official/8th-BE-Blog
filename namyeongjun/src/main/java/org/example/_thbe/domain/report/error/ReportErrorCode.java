package org.example._thbe.domain.report.error;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example._thbe.global.code.BaseCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)

public enum ReportErrorCode implements BaseCode {

    REPORT_ALREADY_EXIST(HttpStatus.BAD_REQUEST,"REPORT400_1","이미 신고한 대상입니다."),
    SELF_REPORT(HttpStatus.BAD_REQUEST,"REPORT400_2","신고할 수 없는 대상입니다."),
    REPORT_ALREADY_HANDLED(HttpStatus.BAD_REQUEST,"REPORT400_3","이미 처리된 신고입니다."),
    REPORT_DELETE_NO_PERMISSION(HttpStatus.FORBIDDEN,"REPORT403_1","취소 권한이 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND,"REPORT404_1","해당 신고가 존재하지 않습니다."),
    UNSUPPORTED_CONTENT_TYPE(HttpStatus.NOT_FOUND,"REPORT404_2","지원하지 않는 대상입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
