package com.example.demo.controller;

import com.example.demo.dto.report.ReportCreateRequest;
import com.example.demo.dto.report.ReportResponse;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(@PathVariable Long postId,
                                                                  @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("게시글 신고가 접수되었습니다.", reportService.reportPost(postId, request)));
    }

    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(@PathVariable Long commentId,
                                                                     @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("댓글 신고가 접수되었습니다.", reportService.reportComment(commentId, request)));
    }

    @PostMapping("/reports/{reportId}/resolve")
    public ApiResponse<ReportResponse> resolveReport(@PathVariable Long reportId) {
        return ApiResponse.ok("신고 처리가 완료되었습니다.", reportService.resolveReport(reportId));
    }
}