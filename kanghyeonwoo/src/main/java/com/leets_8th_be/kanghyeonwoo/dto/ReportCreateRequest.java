package com.leets_8th_be.kanghyeonwoo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(@NotNull @Positive Long reporterId,
                                  @NotBlank @Size(max = 255) String reason) {}
