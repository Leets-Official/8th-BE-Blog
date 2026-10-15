package com.example.demo.controller;

import com.example.demo.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 서비스에서 지정한 HTTP 상태와 오류 메시지를 응답
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ApiErrorResponse(exception.getStatusCode().value(), exception.getReason()));
    }

    // 요청 필드 검증 오류를 400 응답으로 변환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return badRequest(message);
    }

    // 요청 본문이나 파라미터 형식 오류를 400 응답으로 변환
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
        return badRequest("요청 형식이 올바르지 않습니다.");
    }

    // 오류 메시지를 담은 공통 400 응답을 생성
    private ResponseEntity<ApiErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }
}
