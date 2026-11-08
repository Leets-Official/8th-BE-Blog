package org.example._thbe.domain.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.report.controller.docs.ReportControllerDocs;
import org.example._thbe.domain.report.dto.ReportCreateRequest;
import org.example._thbe.domain.report.dto.ReportCreateResponse;
import org.example._thbe.domain.report.service.ReportService;
import org.example._thbe.global.auth.CustomUserDetails;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    //신고 접수 API
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<ReportCreateResponse>> createReport(
            @RequestBody @Valid ReportCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        ReportCreateResponse response = reportService.createReport(request, userDetails.getId());
        return ApiResponse.success(SuccessCode.COMMON_CREATED,response);
    }


    //신고 취소 API
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reportService.deleteReport(reportId, userDetails.getId());
        return ApiResponse.success(SuccessCode.COMMON_OK);
    }



}