package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 처리 요청")
public record ReportResolveRequest(
        @JsonIgnore @Schema(hidden = true) Long processorId,

        @Schema(
                description = "처리 행동. HIDE는 숨김, KEEP은 현재 상태 유지",
                example = "HIDE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ReportResolutionAction action,

        @Schema(
                description = "처리 사유",
                example = "인신공격 확인",
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255)
        String resolutionReason) {}
