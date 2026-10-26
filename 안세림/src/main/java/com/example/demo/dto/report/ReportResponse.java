package com.example.demo.dto.report;

import com.example.demo.entity.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(Long reportId, String targetType, Long targetId, Long reporterId,
                             String reason, ReportStatus status, LocalDateTime createdAt) {
}