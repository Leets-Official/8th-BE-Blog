package com.leets.blog.controller;

import com.leets.blog.dto.report.ReportCreateRequest;
import com.leets.blog.dto.report.ReportResponse;
import com.leets.blog.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("mysql")
@RequiredArgsConstructor
@Tag(name = "신고")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/post/{postId}/reports")
    @Operation(summary = "게시글 신고", description = "동일 사용자의 동일 게시글 중복 신고는 409로 응답합니다.")
    @ApiResponse(responseCode = "201", description = "신고 접수")
    public ResponseEntity<ReportResponse> reportPost(@PathVariable Long postId,
                                                     @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.reportPost(postId, request);
        return ResponseEntity.created(URI.create("/reports/" + response.id())).body(response);
    }

    @PostMapping("/comment/{commentId}/reports")
    @Operation(summary = "댓글 신고", description = "삭제된 댓글과 접근할 수 없는 게시글의 댓글은 신고할 수 없습니다.")
    @ApiResponse(responseCode = "201", description = "신고 접수")
    public ResponseEntity<ReportResponse> reportComment(@PathVariable Long commentId,
                                                        @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.reportComment(commentId, request);
        return ResponseEntity.created(URI.create("/reports/" + response.id())).body(response);
    }

    @PatchMapping("/reports/{reportId}/resolve")
    @Operation(summary = "신고 처리 완료", description = "PENDING → RESOLVED로 변경합니다. 이미 처리된 신고는 409로 응답하며 게시글 숨김은 별도 요청합니다.")
    @ApiResponse(responseCode = "200", description = "처리 완료")
    public ReportResponse resolve(@PathVariable Long reportId) {
        return reportService.resolve(reportId);
    }
}
