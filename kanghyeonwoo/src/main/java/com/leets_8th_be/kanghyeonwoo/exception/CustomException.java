package com.leets_8th_be.kanghyeonwoo.exception;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ApiResponseCode responseCode;

    public CustomException(ApiResponseCode responseCode) {
        super(responseCode.getDefaultMessage());
        this.responseCode = responseCode;
    }

    public CustomException(ApiResponseCode responseCode, String customMessage) {
        super(customMessage);
        this.responseCode = responseCode;
    }
}
