package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글·댓글 신고 접수 요청")
public record ReportCreateRequest(
        @Schema(description = "신고자 사용자 ID", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long reporterId,
        @Schema(description = "신고 사유", example = "욕설이 포함되어 있습니다.", maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 255) String reason) {}
