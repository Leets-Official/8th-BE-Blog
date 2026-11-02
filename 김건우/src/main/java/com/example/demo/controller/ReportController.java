package com.example.demo.controller;

import com.example.demo.dto.ReportRequest;
import com.example.demo.dto.ReportResponse;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 게시글 신고: "신고" 라는 자원을 게시글 하위에 생성 -> CRUD형
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @PathVariable Long postId, @Valid @RequestBody ReportRequest request) {
        return ApiResponse.success(SuccessCode.REPORT_CREATED, reportService.createReport(postId, request));
    }

    // 신고 처리 완료: 상태 전이 + 검증을 묶은 행위 -> 액션형
    @PostMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(@PathVariable Long reportId) {
        return ApiResponse.success(SuccessCode.REPORT_RESOLVED, reportService.resolveReport(reportId));
    }
}
