package com.leets_8th_be.kanghyeonwoo.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "공통 API 응답 규격")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "성공 또는 오류 의미 코드", example = "SUCCESS", implementation = ApiResponseCode.class)
    private ApiResponseCode code;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return success(ApiResponseCode.SUCCESS, ApiResponseCode.SUCCESS.getDefaultMessage(), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(ApiResponseCode.SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> success(ApiResponseCode code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    public static <T> ApiResponse<T> fail(ApiResponseCode code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public static <T> ApiResponse<T> fail(ApiResponseCode code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
