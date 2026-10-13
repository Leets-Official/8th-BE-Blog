package com.leets.blog.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.leets.blog.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@JsonPropertyOrder({"isSuccess", "code", "data", "error"})
public record ApiResponse<T>(
        @JsonProperty("isSuccess") boolean isSuccess,
        String code,
        T data,
        ErrorBody error
) {

    // 200 OK: HTTP 상태와 본문 code를 한 곳에서 함께 정함
    public static <T> ResponseEntity<ApiResponse<T>> okEntity(T data) {
        return ResponseEntity.ok(success(HttpStatus.OK, data));
    }

    // 201 Created
    public static <T> ResponseEntity<ApiResponse<T>> createdEntity(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success(HttpStatus.CREATED, data));
    }

    private static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(true, String.valueOf(status.value()), data, null);
    }

    // 명세에 정의된 에러
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return fail(errorCode.getStatus(), errorCode.name(), errorCode.getMessage());
    }

    // 명세 밖의 스프링 기본 에러 (없는 URL, 지원하지 않는 메서드 등)
    public static ApiResponse<Void> fail(HttpStatusCode status, String code, String message) {
        return new ApiResponse<>(false, String.valueOf(status.value()), null, new ErrorBody(code, message));
    }
}
