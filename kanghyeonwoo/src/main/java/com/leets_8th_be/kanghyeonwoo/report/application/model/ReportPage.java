package com.leets_8th_be.kanghyeonwoo.report.application.model;

import java.util.List;

public record ReportPage(List<ReportListItem> items, int page, int size,
                         long totalElements, int totalPages) {
}
