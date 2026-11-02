package com.leets.blog.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 요청")
public record ReportCreateRequest(
        @Schema(description = "신고하는 회원 ID", example = "102")
        @NotNull(message = "신고자 ID는 필수 입력 항목입니다.")
        Long reporterId,

        @Schema(description = "신고 사유 (255자 이하)", example = "부적절한 내용이 포함되어 있습니다.")
        @NotBlank(message = "신고 사유는 필수 입력 항목입니다.")
        @Size(max = 255, message = "신고 사유는 255자 이하로 입력해주세요.")
        String reason
) {
}
