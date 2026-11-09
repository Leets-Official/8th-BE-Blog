package com.leets.blog.controller;

import com.leets.blog.dto.report.ReportCreateRequest;
import com.leets.blog.dto.report.ReportResponse;
import com.leets.blog.global.response.ApiResponse;
import com.leets.blog.global.response.SuccessCode;
import com.leets.blog.global.swagger.ApiErrorCodes;
import com.leets.blog.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.leets.blog.global.exception.ErrorCode.COMMENT_NOT_FOUND;
import static com.leets.blog.global.exception.ErrorCode.INVALID_INPUT;
import static com.leets.blog.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.leets.blog.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.leets.blog.global.exception.ErrorCode.REPORT_ALREADY_RESOLVED;
import static com.leets.blog.global.exception.ErrorCode.REPORT_DUPLICATED;
import static com.leets.blog.global.exception.ErrorCode.REPORT_NOT_FOUND;

@Tag(name = "신고", description = "게시글/댓글 신고와 신고 처리 API")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고",
            description = "게시글을 신고합니다. 신고는 PENDING 상태로 생성됩니다. 같은 회원이 같은 게시글을 다시 신고하면 409를 반환합니다. 숨김 처리된 게시글은 신고할 수 없습니다.")
    @ApiErrorCodes({INVALID_INPUT, POST_NOT_FOUND, MEMBER_NOT_FOUND, REPORT_DUPLICATED})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @Parameter(description = "신고할 게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody ReportCreateRequest request) {
        return respond(SuccessCode.REPORT_CREATED, reportService.reportPost(postId, request));
    }

    @Operation(summary = "댓글 신고",
            description = "댓글을 신고합니다. 신고는 PENDING 상태로 생성됩니다. 같은 회원이 같은 댓글을 다시 신고하면 409를 반환합니다. 삭제된 댓글은 신고할 수 없습니다.")
    @ApiErrorCodes({INVALID_INPUT, COMMENT_NOT_FOUND, MEMBER_NOT_FOUND, REPORT_DUPLICATED})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(
            @Parameter(description = "신고할 댓글 ID", example = "1") @PathVariable Long commentId,
            @Valid @RequestBody ReportCreateRequest request) {
        return respond(SuccessCode.REPORT_CREATED, reportService.reportComment(commentId, request));
    }

    @Operation(summary = "신고 처리 완료",
            description = "신고 상태를 PENDING에서 RESOLVED로 바꿉니다. 이미 처리된 신고는 409를 반환합니다.")
    @ApiErrorCodes({REPORT_NOT_FOUND, REPORT_ALREADY_RESOLVED})
    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @Parameter(description = "처리할 신고 ID", example = "1") @PathVariable Long reportId) {
        return respond(SuccessCode.REPORT_RESOLVED, reportService.resolveReport(reportId));
    }

    private <T> ResponseEntity<ApiResponse<T>> respond(SuccessCode successCode, T data) {
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode, data));
    }
}
