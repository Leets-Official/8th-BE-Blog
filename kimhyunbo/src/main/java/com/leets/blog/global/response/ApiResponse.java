package com.leets.blog.global.response;

import com.leets.blog.global.exception.ErrorCode;

public record ApiResponse<T>(boolean success, String code, String message, T data) {

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
