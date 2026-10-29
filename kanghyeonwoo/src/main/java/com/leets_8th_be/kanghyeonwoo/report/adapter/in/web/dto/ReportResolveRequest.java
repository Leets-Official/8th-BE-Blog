package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto;

import jakarta.validation.constraints.*;

public record ReportResolveRequest(@NotNull @Positive Long processorId,
                                   @NotNull ReportResolutionAction action,
                                   @NotBlank @Size(max = 255) String resolutionReason) {}
