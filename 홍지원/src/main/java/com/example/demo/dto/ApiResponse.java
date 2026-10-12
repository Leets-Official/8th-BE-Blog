package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(@JsonProperty("isSuccess") boolean isSuccess,
                             Object code, T data, Error error) {
    public static <T> ApiResponse<T> success(Object code, T data) {
        return new ApiResponse<>(true, code, data, null);
    }

    public static ApiResponse<Void> failure(int status, String code, String message) {
        return new ApiResponse<>(false, String.valueOf(status), null, new Error(code, message));
    }

    public record Error(String code, String message) {}
}
