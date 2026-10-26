package org.example._thbe.domain.report.controller;

import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.report.dto.ReportCreateRequest;
import org.example._thbe.domain.report.dto.ReportCreateResponse;
import org.example._thbe.domain.report.service.ReportService;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    //신고 접수 API
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<ReportCreateResponse>> createReport(
            @RequestBody ReportCreateRequest request,
            @RequestParam Long userId
    ) {
        ReportCreateResponse response = reportService.createReport(request,userId);
        return ApiResponse.success(SuccessCode.COMMON_CREATED,response);
    }


    //신고 취소 API
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long reportId,
            @RequestParam Long userId
    ) {
        reportService.deleteReport(reportId,userId);
        return ApiResponse.success(SuccessCode.COMMON_OK);
    }



}