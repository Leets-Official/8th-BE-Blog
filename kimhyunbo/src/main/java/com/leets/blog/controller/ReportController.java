package com.leets.blog.controller;

import com.leets.blog.dto.report.ReportCreateRequest;
import com.leets.blog.dto.report.ReportResponse;
import com.leets.blog.global.response.ApiResponse;
import com.leets.blog.global.response.SuccessCode;
import com.leets.blog.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportCreateRequest request) {
        return respond(SuccessCode.REPORT_CREATED, reportService.reportPost(postId, request));
    }

    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportCreateRequest request) {
        return respond(SuccessCode.REPORT_CREATED, reportService.reportComment(commentId, request));
    }

    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(@PathVariable Long reportId) {
        return respond(SuccessCode.REPORT_RESOLVED, reportService.resolveReport(reportId));
    }

    private <T> ResponseEntity<ApiResponse<T>> respond(SuccessCode successCode, T data) {
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode, data));
    }
}
