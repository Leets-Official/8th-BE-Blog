package com.leets.blog.domain.report.web.controller;

import com.leets.blog.common.config.SwaggerConfig;
import com.leets.blog.common.exception.BindErrorCode;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.common.swagger.ApiErrorCodes;
import com.leets.blog.domain.report.service.ReportService;
import com.leets.blog.domain.report.web.dto.request.ReportCreateRequest;
import com.leets.blog.domain.report.web.dto.response.ReportResolveResponse;
import com.leets.blog.domain.report.web.dto.response.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report API", description = "게시글·댓글 신고 및 신고 처리(관리자) API")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "다른 회원의 게시글을 신고합니다. 같은 글은 한 번만 신고할 수 있습니다. reason: SPAM, ABUSE, ADULT, ETC")
    @ApiErrorCodes({ErrorCode.INVALID_POST_ID, ErrorCode.REQUIRED_FIELD_MISSING, ErrorCode.INVALID_REPORT_REASON,
            ErrorCode.REPORT_DETAIL_LENGTH_EXCEED, ErrorCode.CANNOT_REPORT_OWN_CONTENT, ErrorCode.UNAUTHORIZED,
            ErrorCode.POST_NOT_FOUND, ErrorCode.DUPLICATE_REPORT})
    @SecurityRequirement(name = SwaggerConfig.MEMBER_ID)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @Parameter(hidden = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId,
            @RequestBody @Valid ReportCreateRequest request) {
        return ApiResponse.createdEntity(reportService.reportPost(memberId, postId, request));
    }

    @Operation(summary = "댓글 신고", description = "다른 회원의 댓글을 신고합니다. 같은 댓글은 한 번만 신고할 수 있습니다. reason: SPAM, ABUSE, ADULT, ETC")
    @ApiErrorCodes({ErrorCode.INVALID_COMMENT_ID, ErrorCode.REQUIRED_FIELD_MISSING, ErrorCode.INVALID_REPORT_REASON,
            ErrorCode.REPORT_DETAIL_LENGTH_EXCEED, ErrorCode.CANNOT_REPORT_OWN_CONTENT, ErrorCode.UNAUTHORIZED,
            ErrorCode.COMMENT_NOT_FOUND, ErrorCode.DUPLICATE_REPORT})
    @SecurityRequirement(name = SwaggerConfig.MEMBER_ID)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(
            @Parameter(hidden = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_COMMENT_ID) Long commentId,
            @RequestBody @Valid ReportCreateRequest request) {
        return ApiResponse.createdEntity(reportService.reportComment(memberId, commentId, request));
    }

    @Operation(summary = "신고 처리 (관리자)", description = "접수된(PENDING) 신고를 처리 완료(RESOLVED)로 바꿉니다. 이미 처리된 신고는 409입니다.")
    @ApiErrorCodes({ErrorCode.INVALID_REPORT_ID, ErrorCode.UNAUTHORIZED, ErrorCode.ADMIN_ONLY,
            ErrorCode.REPORT_NOT_FOUND, ErrorCode.REPORT_ALREADY_PROCESSED})
    @SecurityRequirement(name = SwaggerConfig.MEMBER_ID)
    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResolveResponse>> resolve(
            @Parameter(hidden = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_REPORT_ID) Long reportId) {
        return ApiResponse.okEntity(reportService.resolve(memberId, reportId));
    }
}
