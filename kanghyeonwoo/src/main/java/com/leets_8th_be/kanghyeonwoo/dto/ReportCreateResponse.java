package com.leets_8th_be.kanghyeonwoo.dto;

public record ReportCreateResponse(Long reportId, ReportTargetType targetType, Long targetId, ReportStatus status) {
}
