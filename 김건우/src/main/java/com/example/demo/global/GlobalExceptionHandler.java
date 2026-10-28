package com.example.demo.global;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustom(CustomException e) {
        return ApiResponse.failure(e.getErrorCode(), null);
    }

    // @RequestBody 검증 실패 (title 비어 있음 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleBodyValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ApiResponse.failure(ErrorCode.INVALID_INPUT, errors);
    }

    // @RequestParam 검증 실패 (page, size 범위)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleParamValidation(ConstraintViolationException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            errors.put(path.substring(path.lastIndexOf('.') + 1), v.getMessage());
        });
        return ApiResponse.failure(ErrorCode.INVALID_INPUT, errors);
    }

    // /posts/abc, ?page=abc 처럼 숫자가 아닌 값
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ApiResponse.failure(ErrorCode.INVALID_INPUT, Map.of(e.getName(), "숫자여야 합니다."));
    }

    // 삭제 시 ?userId 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParam(MissingServletRequestParameterException e) {
        return ApiResponse.failure(ErrorCode.INVALID_INPUT, Map.of(e.getParameterName(), "필수 값입니다."));
    }

    // JSON 형식이 깨졌을 때
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnreadable(HttpMessageNotReadableException e) {
        return ApiResponse.failure(ErrorCode.INVALID_INPUT, Map.of("body", "요청 본문(JSON) 형식이 올바르지 않습니다."));
    }
}