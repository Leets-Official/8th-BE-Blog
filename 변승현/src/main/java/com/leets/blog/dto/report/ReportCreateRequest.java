package com.leets.blog.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportCreateRequest(
        @Schema(hidden = true) Long userId,
        @Schema(description = "신고 사유. 공백 불가, 최대 500자", example = "부적절한 내용입니다.", requiredMode = Schema.RequiredMode.REQUIRED) String reason
) {}
