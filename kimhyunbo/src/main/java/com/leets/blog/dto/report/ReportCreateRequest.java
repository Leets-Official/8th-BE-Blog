package com.leets.blog.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull(message = "신고자 ID는 필수 입력 항목입니다.")
        Long reporterId,

        @NotBlank(message = "신고 사유는 필수 입력 항목입니다.")
        @Size(max = 255, message = "신고 사유는 255자 이하로 입력해주세요.")
        String reason
) {
}
