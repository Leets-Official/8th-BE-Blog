package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = {PostController.class, ModerationController.class})
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public static class ApiException extends RuntimeException {
        private final int status;
        private final String code;

        public ApiException(int status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException exception) {
        return ResponseEntity.status(exception.status).body(
                ApiResponse.failure(exception.status, exception.code, exception.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiResponse<Void>> handleBody(Exception exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        String code = path.endsWith("/visibility") ? "INVALID_VISIBILITY"
                : path.endsWith("/reports") && "POST".equals(request.getMethod()) ? "INVALID_REPORT_REASON"
                : path.matches(".*/reports/[^/]+") && "PATCH".equals(request.getMethod())
                ? "INVALID_REPORT_RESULT" : "REQUIRED_FIELD_MISSING";
        return handleApi(new ApiException(400, code, "요청 본문 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException exception) {
        return handleApi(new ApiException(400, "INVALID_REPORT_STATUS", "신고 상태 파라미터가 필요합니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleType(MethodArgumentTypeMismatchException exception) {
        String code = switch (exception.getName()) {
            case "page" -> "INVALID_PAGE_NUMBER";
            case "size" -> "INVALID_PAGE_SIZE";
            case "status" -> "INVALID_REPORT_STATUS";
            case "comment_id" -> "INVALID_COMMENT_ID";
            case "report_id" -> "INVALID_REPORT_ID";
            default -> "INVALID_POST_ID";
        };
        return handleApi(new ApiException(400, code, "요청 값의 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("게시글 API 처리 실패: {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.internalServerError().body(
                ApiResponse.failure(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
