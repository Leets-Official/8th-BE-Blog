package com.leets_8th_be.kanghyeonwoo.exception;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE, "필수 요청 파라미터가 누락되었습니다.");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        return errorResponse(ex.getResponseCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ApiResponseCode.INVALID_INPUT_VALUE.getDefaultMessage();
        if (ex.getBindingResult().getFieldError() != null) {
            errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE, errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return errorResponse(ApiResponseCode.TYPE_MISMATCH);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiResponseCode code =
                ex.getCause() == null ? ApiResponseCode.MISSING_REQUEST_BODY : ApiResponseCode.INVALID_INPUT_VALUE;
        return errorResponse(code);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return errorResponse(ApiResponseCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        return errorResponse(ApiResponseCode.ENDPOINT_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("처리되지 않은 서버 오류", ex);
        return errorResponse(ApiResponseCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(ApiResponseCode responseCode) {
        return errorResponse(responseCode, responseCode.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(ApiResponseCode responseCode, String message) {
        ApiResponse<Void> response = ApiResponse.fail(responseCode, message);
        return ResponseEntity.status(responseCode.getHttpStatus()).body(response);
    }
}
