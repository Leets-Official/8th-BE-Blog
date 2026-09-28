package com.example.demo.global.exception.dto.response;

import com.example.demo.global.exception.ErrorCode;

public record ErrorResponse(
    int httpStatus, 
    String message, 
    String code,    
    String detailMessage
) {
    public ErrorResponse(ErrorCode errorCode, String detailMessage) {
        this(
            errorCode.getHttpStatus(),
            errorCode.getMessage(),
            errorCode.getCode(),
            detailMessage
        );
    }
}