package com.leets.blog.exception;

import com.leets.blog.controller.PostController;
import com.leets.blog.controller.AuthController;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.leets.blog.controller.ReportController;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(assignableTypes = {PostController.class, ReportController.class, AuthController.class})
public class PostExceptionHandler {
    public record ErrorResponse(int status, String code, String message) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        return error(400, "INVALID_INPUT", "요청값이 올바르지 않습니다. 필수 항목과 입력 형식을 확인해주세요.");
    }

    @ExceptionHandler(PostApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(PostApiException e) {
        return error(e.getStatus(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBody(HttpMessageNotReadableException e) {
        return error(400, "INVALID_REQUEST_FORMAT", "요청 데이터 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParameter(MethodArgumentTypeMismatchException e) {
        if ("postId".equals(e.getName())) {
            return error(400, "INVALID_POST_ID", "올바르지 않은 게시글 ID입니다.");
        }
        if ("commentId".equals(e.getName())) {
            return error(400, "INVALID_COMMENT_ID", "올바르지 않은 댓글 ID입니다.");
        }
        if ("reportId".equals(e.getName())) {
            return error(400, "INVALID_REPORT_ID", "올바르지 않은 신고 ID입니다.");
        }
        return error(400, "INVALID_REQUEST_FORMAT", "요청 데이터 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null
                    && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_report_reporter_")) {
                return error(409, "DUPLICATE_REPORT", "이미 신고한 대상입니다.");
            }
        }
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null) {
                String name = violation.getConstraintName().toLowerCase(Locale.ROOT);
                if (name.contains("uk_member_email")) return error(409, "DUPLICATE_EMAIL", "이미 가입된 이메일입니다.");
                if (name.contains("uk_member_nickname")) return error(409, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
            }
        }
        log.error("데이터 제약조건 위반", e);
        return error(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse response
                && response.getStatusCode().is4xxClientError()) {
            return error(response.getStatusCode().value(), "INVALID_REQUEST_FORMAT", "요청 형식이 올바르지 않습니다.");
        }
        log.error("게시글 API 처리 중 오류", e);
        return error(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, code, message));
    }
}
