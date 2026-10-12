package com.leets.blog.exception;

import com.leets.blog.controller.PostController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(assignableTypes = PostController.class)
public class PostExceptionHandler {
    public record ErrorResponse(int status, String code, String message) {}

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
        return error(400, "INVALID_REQUEST_FORMAT", "요청 데이터 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("게시글 API 처리 중 오류", e);
        return error(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, code, message));
    }
}
