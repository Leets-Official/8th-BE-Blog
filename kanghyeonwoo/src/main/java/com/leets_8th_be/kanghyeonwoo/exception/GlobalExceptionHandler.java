package com.leets_8th_be.kanghyeonwoo.exception;

import com.leets_8th_be.kanghyeonwoo.dto.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostAccessDenied;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.InvalidCommentQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DuplicateReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.SelfReportForbidden;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportRequest;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedCommentReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(
                ErrorCode.INVALID_INPUT_VALUE.getCode(), "필수 요청 파라미터가 누락되었습니다."));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), ex.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AuthorNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorNotFound(AuthorNotFound ex) {
        return errorResponse(ErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(PostNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFound(PostNotFound ex) {
        return errorResponse(ErrorCode.POST_NOT_FOUND);
    }

    @ExceptionHandler(PostAccessDenied.class)
    public ResponseEntity<ApiResponse<Void>> handlePostAccessDenied(PostAccessDenied ex) {
        return errorResponse(ErrorCode.FORBIDDEN_POST_ACCESS);
    }

    @ExceptionHandler(InvalidCommentQuery.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCommentQuery(InvalidCommentQuery ex) {
        return errorResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(CommentNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFound(CommentNotFound ex) {
        return errorResponse(ErrorCode.COMMENT_NOT_FOUND);
    }

    @ExceptionHandler(ReporterNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleReporterNotFound(ReporterNotFound ex) {
        return errorResponse(ErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateReport.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateReport(DuplicateReport ex) {
        return errorResponse(ErrorCode.DUPLICATE_REPORT);
    }

    @ExceptionHandler(SelfReportForbidden.class)
    public ResponseEntity<ApiResponse<Void>> handleSelfReportForbidden(SelfReportForbidden ex) {
        return errorResponse(ErrorCode.SELF_REPORT_FORBIDDEN);
    }

    @ExceptionHandler(InvalidReportRequest.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidReportRequest(InvalidReportRequest ex) {
        return errorResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(DeletedCommentReport.class)
    public ResponseEntity<ApiResponse<Void>> handleDeletedCommentReport(DeletedCommentReport ex) {
        return errorResponse(ErrorCode.DELETED_COMMENT_REPORT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ErrorCode.INVALID_INPUT_VALUE.getMessage();
        if (ex.getBindingResult().getFieldError() != null) {
            errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.TYPE_MISMATCH.getCode(), ErrorCode.TYPE_MISMATCH.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorCode code = ex.getCause() == null ? ErrorCode.MISSING_REQUEST_BODY : ErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Void> response = ApiResponse.fail(code.getCode(), code.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.METHOD_NOT_ALLOWED.getCode(), ErrorCode.METHOD_NOT_ALLOWED.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null
                    && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_report_reporter_target")) {
                ErrorCode code = ErrorCode.DUPLICATE_REPORT;
                return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.fail(code.getCode(), code.getMessage()));
            }
        }
        return handleGeneralException(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("처리되지 않은 서버 오류", ex);
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(ErrorCode errorCode) {
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
