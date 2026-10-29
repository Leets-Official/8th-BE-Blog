package org.example._thbe.domain.report.controller;


import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.report.dto.ReportListResponse;
import org.example._thbe.domain.report.enums.ReportStatus;
import org.example._thbe.domain.report.service.ReportAdminService;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportAdminService reportAdminService;

    //신고 목록 조회 API
    @GetMapping
    public ResponseEntity<ApiResponse<ReportListResponse>> getReportList(
            @RequestParam ReportStatus status
    ) {
        ReportListResponse response = reportAdminService.getReportList(status);
        return ApiResponse.success(SuccessCode.COMMON_OK,response);
    }

    //신고 승인 API
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveReport(@PathVariable Long reportId) {
        reportAdminService.approveReport(reportId);
        return ApiResponse.success(SuccessCode.COMMON_OK);
    }


    //신고 반려 API
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectReport(@PathVariable Long reportId) {
        reportAdminService.rejectReport(reportId);
        return ApiResponse.success(SuccessCode.COMMON_OK);
    }


}
