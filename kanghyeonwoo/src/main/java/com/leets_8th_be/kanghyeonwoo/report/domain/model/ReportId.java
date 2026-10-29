package com.leets_8th_be.kanghyeonwoo.report.domain.model;

public record ReportId(Long value) {
    public ReportId {
        if (value == null) throw new IllegalArgumentException("신고 ID는 필수입니다.");
    }
}
