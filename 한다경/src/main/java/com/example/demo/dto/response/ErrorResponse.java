package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "공통 에러 응답. code와 message로 오류를 구분하며, 필드 검증 실패 시 errors를 포함합니다.")
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final Integer status;
    @Schema(description = "에러 구분 코드", example = "INVALID_INPUT")
    private final String code;
    @Schema(description = "에러 설명", example = "요청값이 올바르지 않습니다.")
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "필드별 검증 오류. 오류가 없으면 응답에서 생략합니다.")
    private final List<ValidationError> errors;

    public ErrorResponse(int status, String code, String message) {
        this(status, code, message, List.of());
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {

        @Schema(description = "검증에 실패한 필드", example = "title")
        private final String field;
        @Schema(description = "에러 설명", example = "요청값이 올바르지 않습니다.")
        private final String message;
    }
}
