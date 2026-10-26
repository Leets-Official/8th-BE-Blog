package com.leets.mission.domain.report.entity;

// 신고 상태 (PENDING: 처리 대기, RESOLVED: 신고 승인, REJECTED: 신고 반려)
public enum ReportStatus {
    PENDING,
    RESOLVED,
    REJECTED
}
