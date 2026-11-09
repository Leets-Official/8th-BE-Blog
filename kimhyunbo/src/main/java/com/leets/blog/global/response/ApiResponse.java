package com.leets.blog.global.response;

import com.leets.blog.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 형식")
public record ApiResponse<T>(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 코드", example = "S200")
        String code,

        @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
        String message,

        @Schema(description = "응답 데이터")
        T data
) {

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return fail(errorCode, null);
    }
}
