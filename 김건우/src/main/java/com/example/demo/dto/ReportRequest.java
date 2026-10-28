package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @NotBlank(message = "신고 사유를 입력해주세요.")
        @Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.") String reason
) {}
