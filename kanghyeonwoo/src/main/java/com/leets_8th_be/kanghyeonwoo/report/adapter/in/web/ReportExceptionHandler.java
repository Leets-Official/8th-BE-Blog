package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.report.application.exception.CommentTargetNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedCommentReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.DuplicateReport;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.InvalidReportRequest;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ParentPostUnavailable;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.PostTargetNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReportNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.SelfReportForbidden;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAlreadyResolved;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = ReportController.class)
public class ReportExceptionHandler {

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

    @ExceptionHandler(ReportAlreadyResolved.class)
    public ResponseEntity<ApiResponse<Void>> handleReportAlreadyResolved(ReportAlreadyResolved ex) {
        return errorResponse(ApiResponseCode.REPORT_ALREADY_RESOLVED);
    }

    @ExceptionHandler(DeletedReportTarget.class)
    public ResponseEntity<ApiResponse<Void>> handleDeletedReportTarget(DeletedReportTarget ex) {
        return errorResponse(ApiResponseCode.RESOURCE_DELETED, "삭제된 대상은 숨길 수 없습니다.");
    }

    @ExceptionHandler(PostTargetNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handlePostTargetNotFound(PostTargetNotFound ex) {
        return errorResponse(ApiResponseCode.POST_NOT_FOUND);
    }

    @ExceptionHandler(ParentPostUnavailable.class)
    public ResponseEntity<ApiResponse<Void>> handleParentPostUnavailable(ParentPostUnavailable ex) {
        return errorResponse(ApiResponseCode.POST_NOT_FOUND);
    }

    @ExceptionHandler(CommentTargetNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentTargetNotFound(CommentTargetNotFound ex) {
        return errorResponse(ApiResponseCode.COMMENT_NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null
                    && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_report_reporter_target")) {
                return errorResponse(ApiResponseCode.DUPLICATE_RESOURCE);
            }
        }
        log.error("처리되지 않은 데이터 무결성 오류", ex);
        return errorResponse(ApiResponseCode.INTERNAL_SERVER_ERROR);
    }

    private static ResponseEntity<ApiResponse<Void>> errorResponse(ApiResponseCode code) {
        return errorResponse(code, code.getDefaultMessage());
    }

    private static ResponseEntity<ApiResponse<Void>> errorResponse(ApiResponseCode code, String message) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.fail(code, message));
    }
}
