package com.example.demo.report.exception;

import com.example.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {
    ALREADY_REPORTED(HttpStatus.CONFLICT,
            "REPORT4001",
            "이미 신고한 게시글입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "REPORT4002",
            "신고 내역을 찾을 수 없습니다."
    ),
    ALREADY_PROCESSED(
            HttpStatus.CONFLICT,
            "REPORT4003",
            "이미 처리된 신고입니다."
    ),
    INVALID_REPORT_STATUS(
            HttpStatus.BAD_REQUEST,
            "REPORT4004",
            "변경할 수 없는 신고 상태입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
