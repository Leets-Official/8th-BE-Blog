package com.example.demo.global.apiPayload.handler;

import com.example.demo.global.apiPayload.ApiResponse;
import com.example.demo.global.apiPayload.code.BaseErrorCode;
import com.example.demo.global.apiPayload.code.GeneralErrorCode;
import com.example.demo.global.apiPayload.exception.GeneralException;
import com.example.demo.post.dto.PostReqDTO;
import com.example.demo.post.exception.PostErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException ex) {
        return ResponseEntity.status(ex.getCode().getStatus())
                .body(ApiResponse.onFailure(ex.getCode(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // 게시글 요청은 기존 Swagger 문서에 적힌 응답 그대로 유지
        Object target = ex.getBindingResult().getTarget();
        if (target != null && target.getClass().getEnclosingClass() == PostReqDTO.class) {
            FieldError first = ex.getBindingResult().getFieldErrors().get(0);
            BaseErrorCode code = ("title".equals(first.getField()) && "Size".equals(first.getCode()))
                    ? PostErrorCode.TITLE_TOO_LONG
                    : PostErrorCode.INVALID_INPUT;
            return ResponseEntity.status(code.getStatus())
                    .body(ApiResponse.onFailure(code, null));
        }

        // 나머지는 필드별로 DTO에 적은 message를 그대로 내려줌
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        BaseErrorCode code = GeneralErrorCode.INVALID_REQUEST_FORMAT;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);

        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }
}
