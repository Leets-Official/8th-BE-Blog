package com.leets.blog.controller;

import com.leets.blog.dto.report.ReportCreateRequest;
import com.leets.blog.dto.report.ReportResponse;
import com.leets.blog.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "신고", description = "신고 접수 및 처리 상태 변경. userId는 인증 대신 사용하는 과제용 식별값입니다.")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/post/{postId}/reports")
    @Operation(summary = "게시글 신고", description = "실제로 존재하는 사용자와 게시글 ID가 필요합니다. PENDING 상태로 접수하며, 처리 완료 후에도 동일 사용자의 동일 게시글 중복 신고는 409로 응답합니다.")
    @ApiResponse(responseCode = "201", description = "신고 접수")
    public ResponseEntity<ReportResponse> reportPost(@Parameter(description = "신고할 게시글 ID", example = "1") @PathVariable Long postId,
                                                     @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.reportPost(postId, request);
        return ResponseEntity.created(URI.create("/reports/" + response.id())).body(response);
    }

    @PostMapping("/comment/{commentId}/reports")
    @Operation(summary = "댓글 신고", description = "PENDING 상태로 접수합니다. 동일 사용자의 동일 댓글 중복 신고는 409로 응답합니다. 삭제된 댓글과 접근할 수 없는 게시글의 댓글은 신고할 수 없습니다.")
    @ApiResponse(responseCode = "201", description = "신고 접수")
    public ResponseEntity<ReportResponse> reportComment(@Parameter(description = "신고할 댓글 ID", example = "1") @PathVariable Long commentId,
                                                        @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.reportComment(commentId, request);
        return ResponseEntity.created(URI.create("/reports/" + response.id())).body(response);
    }

    @PatchMapping("/reports/{reportId}/resolve")
    @Operation(summary = "신고 처리 완료", description = "요청 본문 없이 PENDING → RESOLVED로 변경합니다. 이미 처리된 신고는 409로 응답하며 게시글 숨김은 별도 요청합니다.")
    @ApiResponse(responseCode = "200", description = "처리 완료")
    public ReportResponse resolve(@Parameter(description = "처리할 신고 ID", example = "1") @PathVariable Long reportId) {
        return reportService.resolve(reportId);
    }
}
