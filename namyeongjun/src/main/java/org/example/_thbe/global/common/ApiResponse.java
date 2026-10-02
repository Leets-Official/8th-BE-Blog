package org.example._thbe.global.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example._thbe.global.code.BaseCode;
import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"isSuccess","code","message","data"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ResponseEntity<ApiResponse<T>> success(BaseCode baseCode,T data) {
        return of(true,baseCode,data);
    }

    public static ResponseEntity<ApiResponse<Void>> success(BaseCode baseCode) {
        return of(true,baseCode,null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(BaseCode baseCode,T data) {
        return of(false, baseCode, data);
    }

    public static ResponseEntity<ApiResponse<Void>> failure(BaseCode baseCode) {
        return of(false, baseCode, null);
    }


    private static <T> ResponseEntity<ApiResponse<T>> of(
            boolean success,
            BaseCode baseCode,
            T data
    ) {
        return ResponseEntity
                .status(baseCode.getStatus())
                .body(new ApiResponse<>(success, baseCode.getCode(), baseCode.getMessage(), data));
    }
}
