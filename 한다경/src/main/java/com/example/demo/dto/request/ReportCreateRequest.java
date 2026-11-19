package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ReportCreateRequest(
        @Schema(description = "신고 사유 (필수, 공백 불가, 최대 255자)", example = "부적절한 내용입니다")
        @NotBlank(message = "신고 사유는 필수입니다.")
        @Size(max = 255, message = "신고 사유는 255자를 초과할 수 없습니다.")
        String reason
) {
}
