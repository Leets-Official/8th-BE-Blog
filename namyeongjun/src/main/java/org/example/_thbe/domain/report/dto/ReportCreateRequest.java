package org.example._thbe.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example._thbe.domain.report.enums.ReportContentType;

public record ReportCreateRequest(
            @NotBlank(message = "신고 사유를 기재해주세요.")
            String reason,

            @NotNull(message = "컨텐츠 타입 미입력")
            ReportContentType contentType,

            @NotNull(message = "컨텐츠 아이디 미입력")
            Long contentId
    ) {

    }

