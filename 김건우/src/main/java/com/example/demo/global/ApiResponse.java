package com.example.demo.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        @JsonProperty("isSuccess") boolean isSuccess,
        String code,
        String message,
        T data
) {
    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessCode code, T data) {
        return ResponseEntity.status(code.getStatus())
                .body(new ApiResponse<>(true, code.getCode(), code.getMessage(), data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(ErrorCode code, T data) {
        return ResponseEntity.status(code.getStatus())
                .body(new ApiResponse<>(false, code.getCode(), code.getMessage(), data));
    }
}