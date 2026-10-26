package com.leets.blog.global.exception;

import com.leets.blog.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스에서 직접 던진 예외 (게시글 없음, 권한 없음 등)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        return toResponse(e.getErrorCode(), null);
    }

    // @Valid 요청 본문 검증 실패 (제목 공백 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorResponse>>> handleValidation(MethodArgumentNotValidException e) {
        List<FieldErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return toResponse(ErrorCode.INVALID_INPUT, errors);
    }

    // @RequestParam, @PathVariable 검증 실패 (page=0 등)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorResponse>>> handleParameterValidation(HandlerMethodValidationException e) {
        List<FieldErrorResponse> errors = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldErrorResponse(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return toResponse(ErrorCode.INVALID_INPUT, errors);
    }

    // 필수 쿼리 파라미터 누락 (삭제 시 authorId 누락 등)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorResponse>>> handleMissingParameter(MissingServletRequestParameterException e) {
        List<FieldErrorResponse> errors = List.of(new FieldErrorResponse(e.getParameterName(), "필수 입력 항목입니다."));
        return toResponse(ErrorCode.INVALID_INPUT, errors);
    }

    // 숫자 자리에 문자가 들어온 경우 (/posts/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(ErrorCode.TYPE_MISMATCH, null);
    }

    // 요청 본문이 없거나 JSON 형식이 깨진 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.EMPTY_BODY, null);
    }

    // 존재하지 않는 경로
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return toResponse(ErrorCode.NOT_FOUND, null);
    }

    // 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED, null);
    }

    // 그 외 예상하지 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
    }

    private <T> ResponseEntity<ApiResponse<T>> toResponse(ErrorCode errorCode, T data) {
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode, data));
    }
}
