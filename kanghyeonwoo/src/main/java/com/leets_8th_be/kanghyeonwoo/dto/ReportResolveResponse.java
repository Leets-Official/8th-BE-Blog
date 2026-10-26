package com.leets_8th_be.kanghyeonwoo.dto;

import java.time.LocalDateTime;

public record ReportResolveResponse(Long reportId, ReportStatus status, ReportResolutionAction action,
                                    Long processorId, LocalDateTime resolvedAt,
                                    boolean targetAvailable, Boolean targetHidden) {
}
