package com.leets.mission.global.apiPayload.exception;

import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

// 컨트롤러에서 발생한 예외를 공통 실패 응답(ApiResponse)으로 변환하는 전역 예외 처리기
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스에서 던진 커스텀 예외-게시글없음.사용자 찾지 못함
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        return toResponse(e.getErrorStatus(), null);
    }

    // @Valid 검증 실패 → 어떤 필드가 왜 틀렸는지 result에
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            // 한 필드에 여러 오류가 있으면 첫 번째 메시지만 사용
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return toResponse(ErrorStatus.COMMON_400_1, errors);
    }

    // 경로 변수,쿼리 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return toResponse(ErrorStatus.COMMON_400_1, Map.of(e.getName(), "올바른 형식이 아닙니다."));
    }

    // 삭제 시 userId 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(MissingServletRequestParameterException e) {
        if ("userId".equals(e.getParameterName())) {
            return toResponse(ErrorStatus.COMMON_400_2, null);
        }
        return toResponse(ErrorStatus.COMMON_400_1, null);
    }

    // 요청 본문을 읽을 수 없음
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadableException(HttpMessageNotReadableException e) {
        return toResponse(ErrorStatus.COMMON_400_1, null);
    }

    // 그 외 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) throws Exception {
        // 없는 URL(404), 지원하지 않는 HTTP 메서드(405)
        if (e instanceof ErrorResponse) {
            throw e;
        }
        log.error("처리되지 않은 예외 발생", e);
        return toResponse(ErrorStatus.COMMON_500_1, null);
    }

    private <T> ResponseEntity<ApiResponse<T>> toResponse(ErrorStatus status, T result) {
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ApiResponse.onFailure(status, result));
    }
}
