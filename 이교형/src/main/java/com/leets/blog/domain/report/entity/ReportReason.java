package com.leets.blog.domain.report.entity;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;

import java.util.Arrays;

public enum ReportReason {
    SPAM, // 스팸, 홍보글
    ABUSE, // 욕설, 비방
    ADULT, // 선정성
    ETC; // 기타 -> 내용은 detail에

    // 요청 body의 문자열을 enum으로 변환, 없는 값이면 400
    public static ReportReason from(String value) {
        return Arrays.stream(values())
                .filter(reason -> reason.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REPORT_REASON));
    }
}
