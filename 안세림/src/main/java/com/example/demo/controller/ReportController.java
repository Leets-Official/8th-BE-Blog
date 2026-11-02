package com.example.demo.controller;

import com.example.demo.dto.report.ReportCreateRequest;
import com.example.demo.dto.report.ReportResponse;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다(PENDING). 같은 사용자가 같은 글을 다시 신고하면 409(REPORT4091)를 반환합니다.")
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(@PathVariable Long postId,
                                                                  @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("게시글 신고가 접수되었습니다.", reportService.reportPost(postId, request)));
    }

    @Operation(summary = "댓글 신고", description = "댓글을 신고합니다(PENDING). 같은 사용자가 같은 댓글을 다시 신고하면 409(REPORT4091)를 반환합니다.")
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(@PathVariable Long commentId,
                                                                     @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("댓글 신고가 접수되었습니다.", reportService.reportComment(commentId, request)));
    }

    @Operation(summary = "신고 처리 완료", description = "신고 상태를 PENDING에서 RESOLVED로 바꿉니다. 이미 처리된 신고면 409(REPORT4092)를 반환합니다.")
    @PostMapping("/reports/{reportId}/resolve")
    public ApiResponse<ReportResponse> resolveReport(@PathVariable Long reportId) {
        return ApiResponse.ok("신고 처리가 완료되었습니다.", reportService.resolveReport(reportId));
    }
}