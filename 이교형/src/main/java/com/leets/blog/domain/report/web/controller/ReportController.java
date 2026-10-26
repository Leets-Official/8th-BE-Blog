package com.leets.blog.domain.report.web.controller;

import com.leets.blog.common.exception.BindErrorCode;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.domain.report.service.ReportService;
import com.leets.blog.domain.report.web.dto.request.ReportCreateRequest;
import com.leets.blog.domain.report.web.dto.response.ReportResolveResponse;
import com.leets.blog.domain.report.web.dto.response.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report API", description = "게시글·댓글 신고 및 신고 처리(관리자) API")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "다른 회원의 게시글을 신고합니다. 같은 글은 한 번만 신고할 수 있습니다. reason: SPAM, ABUSE, ADULT, ETC")
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @Parameter(description = "신고하는 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId,
            @RequestBody @Valid ReportCreateRequest request) {
        return ApiResponse.createdEntity(reportService.reportPost(memberId, postId, request));
    }

    @Operation(summary = "댓글 신고", description = "다른 회원의 댓글을 신고합니다. 같은 댓글은 한 번만 신고할 수 있습니다. reason: SPAM, ABUSE, ADULT, ETC")
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(
            @Parameter(description = "신고하는 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_COMMENT_ID) Long commentId,
            @RequestBody @Valid ReportCreateRequest request) {
        return ApiResponse.createdEntity(reportService.reportComment(memberId, commentId, request));
    }

    @Operation(summary = "신고 처리 (관리자)", description = "접수된(PENDING) 신고를 처리 완료(RESOLVED)로 바꿉니다. 이미 처리된 신고는 409입니다.")
    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResolveResponse>> resolve(
            @Parameter(description = "관리자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_REPORT_ID) Long reportId) {
        return ApiResponse.okEntity(reportService.resolve(memberId, reportId));
    }
}
