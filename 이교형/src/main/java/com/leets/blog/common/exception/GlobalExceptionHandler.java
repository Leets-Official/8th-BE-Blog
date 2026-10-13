package com.leets.blog.common.exception;

import com.leets.blog.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스에서 직접 던진 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        return toResponse(e.getErrorCode());
    }

    // @Valid 요청 body 검증 실패: 어노테이션 message에 적은 ErrorCode 이름으로 응답
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        ErrorCode errorCode = e.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorCode)
                .min(Comparator.comparingInt(ErrorCode::ordinal))   // 여러 개면 ErrorCode 선언 순서가 앞선 것
                .orElse(ErrorCode.REQUIRED_FIELD_MISSING);
        return toResponse(errorCode);
    }

    // 숫자 자리에 문자가 들어온 경우 (/posts/abc, ?page=abc, X-Member-Id: abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(resolveBindError(e.getParameter()));
    }

    // 필수 헤더가 없는 경우 (X-Member-Id 누락 등)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        return toResponse(resolveBindError(e.getParameter()));
    }

    // body가 비었거나 JSON 형식이 깨진 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.REQUIRED_FIELD_MISSING);
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 없는 URL(404), 지원하지 않는 메서드(405) 같은 스프링 기본 에러는 원래 상태 코드를 유지
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            HttpStatus httpStatus = HttpStatus.valueOf(status.value());
            return ResponseEntity.status(status)
                    .body(ApiResponse.fail(status, httpStatus.name(), httpStatus.getReasonPhrase()));
        }

        log.error("처리되지 않은 예외", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    private ErrorCode toErrorCode(FieldError fieldError) {
        try {
            return ErrorCode.valueOf(fieldError.getDefaultMessage());
        } catch (IllegalArgumentException | NullPointerException e) {
            // message 지정을 빠뜨렸거나 오타가 난 경우
            log.warn("ErrorCode로 변환할 수 없는 검증 message: field={}, message={}",
                    fieldError.getField(), fieldError.getDefaultMessage());
            return ErrorCode.REQUIRED_FIELD_MISSING;
        }
    }

    // 파라미터에 붙은 @BindErrorCode 값, 없으면 기본값
    private ErrorCode resolveBindError(MethodParameter parameter) {
        BindErrorCode annotation = parameter.getParameterAnnotation(BindErrorCode.class);
        return annotation != null ? annotation.value() : ErrorCode.REQUIRED_FIELD_MISSING;
    }
}
