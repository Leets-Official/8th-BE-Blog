package com.leets.choyeji.dto;

public record ApiResponse<T>(boolean isSuccess, String code, String message, T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "COMMON_200_1", "요청 응답이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> failure(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
