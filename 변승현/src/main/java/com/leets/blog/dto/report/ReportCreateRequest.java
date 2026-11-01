package com.leets.blog.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportCreateRequest(
        @Schema(description = "과제용 신고자 ID", example = "1") Long userId,
        @Schema(description = "신고 사유, 최대 500자", example = "부적절한 내용입니다.") String reason
) {}
