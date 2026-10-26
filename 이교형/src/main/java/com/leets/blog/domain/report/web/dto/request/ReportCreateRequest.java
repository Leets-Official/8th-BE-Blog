package com.leets.blog.domain.report.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 검증 message에는 응답할 ErrorCode 이름을 적음
// reason은 String으로 받아 서비스에서 변환
@Schema(description = "신고 요청")
public record ReportCreateRequest(

        @Schema(description = "신고 사유 (SPAM, ABUSE, ADULT, ETC)", example = "SPAM")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        String reason,

        @Schema(description = "상세 내용 (선택, 500자 이하)", example = "광고 글입니다.")
        @Size(max = 500, message = "REPORT_DETAIL_LENGTH_EXCEED")
        String detail
) {
}
