package com.leets.mission.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 오류 응답")
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드")
    private final int status;

    @Schema(description = "오류 코드")
    private final String code;

    @Schema(description = "오류 메시지")
    private final String message;

    @Schema(description = "필드별 검증 오류")
    private final List<FieldErrorDetail> errors;

    @Schema(description = "오류 발생 시각")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> errors) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "개별 필드 검증 오류")
    public static class FieldErrorDetail {
        @Schema(description = "오류가 발생한 필드")
        private String field;

        @Schema(description = "오류 사유")
        private String reason;
    }
}