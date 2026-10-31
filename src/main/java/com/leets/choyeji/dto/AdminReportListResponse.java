package com.leets.choyeji.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record AdminReportListResponse(List<AdminReportResponse> reports, int page, int size,
                                      long totalElements, int totalPages, boolean hasNext) {
    public static AdminReportListResponse from(Page<AdminReportResponse> result) {
        return new AdminReportListResponse(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }
}
