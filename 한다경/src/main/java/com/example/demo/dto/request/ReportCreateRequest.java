package com.example.demo.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ReportCreateRequest(
        @NotBlank(message = "신고 사유는 필수입니다.")
        @Size(max = 255, message = "신고 사유는 255자를 초과할 수 없습니다.") String reason
) {
}
