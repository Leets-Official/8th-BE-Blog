package com.leets_8th_be.kanghyeonwoo.exception;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
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
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReportNotFound;
import com.leets_8th_be.kanghyeonwoo.search.application.exception.SearchKeywordNotFound;
import com.leets_8th_be.kanghyeonwoo.search.application.exception.InvalidSearchRequest;
import com.leets_8th_be.kanghyeonwoo.search.domain.InvalidSearchKeyword;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAlreadyResolved;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE, "필수 요청 파라미터가 누락되었습니다.");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        return errorResponse(ex.getResponseCode(), ex.getMessage());
    }

    @ExceptionHandler(AuthorNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorNotFound(AuthorNotFound ex) {
        return errorResponse(ApiResponseCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(PostNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFound(PostNotFound ex) {
        return errorResponse(ApiResponseCode.POST_NOT_FOUND);
    }

    @ExceptionHandler(PostAccessDenied.class)
    public ResponseEntity<ApiResponse<Void>> handlePostAccessDenied(PostAccessDenied ex) {
        return errorResponse(ApiResponseCode.POST_ACCESS_DENIED);
    }

    @ExceptionHandler(InvalidCommentQuery.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCommentQuery(InvalidCommentQuery ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(CommentNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFound(CommentNotFound ex) {
        return errorResponse(ApiResponseCode.COMMENT_NOT_FOUND);
    }

    @ExceptionHandler(ReporterNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleReporterNotFound(ReporterNotFound ex) {
        return errorResponse(ApiResponseCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateReport.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateReport(DuplicateReport ex) {
        return errorResponse(ApiResponseCode.DUPLICATE_RESOURCE);
    }

    @ExceptionHandler(SelfReportForbidden.class)
    public ResponseEntity<ApiResponse<Void>> handleSelfReportForbidden(SelfReportForbidden ex) {
        return errorResponse(ApiResponseCode.SELF_REPORT_FORBIDDEN);
    }

    @ExceptionHandler(InvalidReportRequest.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidReportRequest(InvalidReportRequest ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(DeletedCommentReport.class)
    public ResponseEntity<ApiResponse<Void>> handleDeletedCommentReport(DeletedCommentReport ex) {
        return errorResponse(ApiResponseCode.RESOURCE_DELETED, "삭제된 댓글은 신고할 수 없습니다.");
    }

    @ExceptionHandler(InvalidReportQuery.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidReportQuery(InvalidReportQuery ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(ReportNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleReportNotFound(ReportNotFound ex) {
        return errorResponse(ApiResponseCode.REPORT_NOT_FOUND);
    }

    @ExceptionHandler(SearchKeywordNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchKeywordNotFound(SearchKeywordNotFound ex) {
        return errorResponse(ApiResponseCode.SEARCH_KEYWORD_NOT_FOUND);
    }

    @ExceptionHandler(InvalidSearchRequest.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSearchRequest(InvalidSearchRequest ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(InvalidSearchKeyword.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSearchKeyword(InvalidSearchKeyword ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(ReportAlreadyResolved.class)
    public ResponseEntity<ApiResponse<Void>> handleReportAlreadyResolved(ReportAlreadyResolved ex) {
        return errorResponse(ApiResponseCode.REPORT_ALREADY_RESOLVED);
    }

    @ExceptionHandler(DeletedReportTarget.class)
    public ResponseEntity<ApiResponse<Void>> handleDeletedReportTarget(DeletedReportTarget ex) {
        return errorResponse(ApiResponseCode.RESOURCE_DELETED, "삭제된 대상은 숨길 수 없습니다.");
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
        ApiResponseCode code = ex.getCause() == null
                ? ApiResponseCode.MISSING_REQUEST_BODY : ApiResponseCode.INVALID_INPUT_VALUE;
        return errorResponse(code);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return errorResponse(ApiResponseCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        return errorResponse(ApiResponseCode.ENDPOINT_NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null
                    && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_report_reporter_target")) {
                return errorResponse(ApiResponseCode.DUPLICATE_RESOURCE);
            }
        }
        return handleGeneralException(ex);
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
