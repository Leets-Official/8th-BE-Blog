package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.COMMENT_NOT_FOUND;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.DUPLICATE_RESOURCE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.MISSING_REQUEST_BODY;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.POST_NOT_FOUND;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.REPORT_ALREADY_RESOLVED;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.REPORT_NOT_FOUND;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.RESOURCE_DELETED;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.SELF_REPORT_FORBIDDEN;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.TYPE_MISMATCH;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.USER_NOT_FOUND;

import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportCreateRequest;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportCreateResponse;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportResolutionAction;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportResolveRequest;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportResolveResponse;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportResponse;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportStatus;
import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportTargetType;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportListItem;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportResolutionView;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.GetReportsUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportSearchQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportUseCase;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.shared.security.CurrentUser;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import com.leets_8th_be.kanghyeonwoo.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Report", description = "신고 접수·조회·처리 API")
@RequestMapping(ApiPaths.API_V2)
public class ReportController {
    private final CurrentUser currentUser;
    private final ReportPostUseCase reportPostUseCase;
    private final ReportCommentUseCase reportCommentUseCase;
    private final GetReportsUseCase getReportsUseCase;
    private final ResolveReportUseCase resolveReportUseCase;

    @PostMapping("/posts/{postId}/reports")
    @Operation(summary = "게시글 신고", description = "본인·숨김 게시글은 신고할 수 없으며 동일 사용자의 중복 신고는 409입니다.")
    @ApiErrorCodes({
        INVALID_INPUT_VALUE,
        TYPE_MISMATCH,
        MISSING_REQUEST_BODY,
        SELF_REPORT_FORBIDDEN,
        USER_NOT_FOUND,
        POST_NOT_FOUND,
        DUPLICATE_RESOURCE
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "게시글 신고 접수 성공",
            useReturnTypeSchema = true)
    public ResponseEntity<ApiResponse<ReportCreateResponse>> reportPost(
            @Parameter(description = "신고할 게시글 ID", example = "1") @PathVariable("postId") Long postId,
            @Valid @RequestBody ReportCreateRequest request) {
        ReportPostCommand command = new ReportPostCommand(postId, currentUser.requireId(), request.reason());
        ReportView report = reportPostUseCase.report(command);
        ReportCreateResponse response = toCreateResponse(report);
        ApiResponse<ReportCreateResponse> body =
                ApiResponse.success(ApiResponseCode.RESOURCE_CREATED, "게시글 신고가 접수되었습니다.", response);
        return ResponseEntity.status(ApiResponseCode.RESOURCE_CREATED.getHttpStatus())
                .body(body);
    }

    @PostMapping("/comments/{commentId}/reports")
    @Operation(summary = "댓글 신고", description = "본인·숨김·삭제 댓글과 숨김 게시글의 댓글은 신고할 수 없습니다. 중복 신고는 409입니다.")
    @ApiErrorCodes({
        INVALID_INPUT_VALUE,
        TYPE_MISMATCH,
        MISSING_REQUEST_BODY,
        SELF_REPORT_FORBIDDEN,
        USER_NOT_FOUND,
        POST_NOT_FOUND,
        COMMENT_NOT_FOUND,
        DUPLICATE_RESOURCE,
        RESOURCE_DELETED
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "댓글 신고 접수 성공",
            useReturnTypeSchema = true)
    public ResponseEntity<ApiResponse<ReportCreateResponse>> reportComment(
            @Parameter(description = "신고할 댓글 ID", example = "1") @PathVariable("commentId") Long commentId,
            @Valid @RequestBody ReportCreateRequest request) {
        ReportCommentCommand command = new ReportCommentCommand(commentId, currentUser.requireId(), request.reason());
        ReportView report = reportCommentUseCase.report(command);
        ReportCreateResponse response = toCreateResponse(report);
        ApiResponse<ReportCreateResponse> body =
                ApiResponse.success(ApiResponseCode.RESOURCE_CREATED, "댓글 신고가 접수되었습니다.", response);
        return ResponseEntity.status(ApiResponseCode.RESOURCE_CREATED.getHttpStatus())
                .body(body);
    }

    @GetMapping("/reports")
    @Operation(summary = "신고 목록·스냅샷 검색", description = "필터는 AND, 검색어는 사유·제목/본문 스냅샷을 OR로 검색합니다. 관리자만 조회할 수 있습니다.")
    @ApiErrorCodes({INVALID_INPUT_VALUE, TYPE_MISMATCH})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "신고 목록 조회 성공",
            useReturnTypeSchema = true)
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse>>> getReports(
            @Parameter(description = "1부터 시작하는 페이지 번호", schema = @Schema(minimum = "1", example = "1"))
                    @RequestParam(name = "page", defaultValue = "1")
                    int page,
            @Parameter(
                            description = "페이지 크기 (최대 100)",
                            schema = @Schema(minimum = "1", maximum = "100", example = "10"))
                    @RequestParam(name = "size", defaultValue = "10")
                    int size,
            @Parameter(description = "신고 상태", example = "PENDING") @RequestParam(name = "status", required = false)
                    ReportStatus status,
            @Parameter(description = "신고 대상 종류", example = "POST") @RequestParam(name = "targetType", required = false)
                    ReportTargetType targetType,
            @Parameter(description = "대상 ID", example = "1") @RequestParam(name = "targetId", required = false)
                    Long targetId,
            @Parameter(description = "사유·제목 스냅샷·본문 스냅샷 검색어", example = "욕설")
                    @RequestParam(name = "keyword", required = false)
                    String keyword) {
        ReportSearchQuery query = new ReportSearchQuery(
                page,
                size,
                status == null ? null : ReportState.valueOf(status.name()),
                targetType == null ? null : ReportTarget.valueOf(targetType.name()),
                targetId,
                keyword);
        ReportPage result = getReportsUseCase.getReports(query);
        PageResponse<ReportResponse> response = new PageResponse<>(
                result.items().stream().map(ReportController::toReportResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
        return ResponseEntity.ok(ApiResponse.success("신고 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/reports/{reportId}/resolve")
    @Operation(summary = "신고 처리", description = "관리자만 처리할 수 있습니다. HIDE는 대상 숨김, KEEP는 현재 공개 상태 유지입니다. 재처리는 409입니다.")
    @ApiErrorCodes({
        INVALID_INPUT_VALUE,
        TYPE_MISMATCH,
        MISSING_REQUEST_BODY,
        USER_NOT_FOUND,
        REPORT_NOT_FOUND,
        REPORT_ALREADY_RESOLVED,
        RESOURCE_DELETED
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "신고 처리 성공",
            useReturnTypeSchema = true)
    public ResponseEntity<ApiResponse<ReportResolveResponse>> resolve(
            @Parameter(description = "처리할 신고 ID", example = "1") @PathVariable("reportId") Long reportId,
            @Valid @RequestBody ReportResolveRequest request) {
        ResolveReportCommand command = new ResolveReportCommand(
                new ReportId(reportId),
                currentUser.requireId(),
                ReportAction.valueOf(request.action().name()),
                request.resolutionReason());
        ReportResolutionView result = resolveReportUseCase.resolve(command);
        ReportResolveResponse response = toResolveResponse(result);
        return ResponseEntity.ok(ApiResponse.success("신고 처리가 완료되었습니다.", response));
    }

    private static ReportCreateResponse toCreateResponse(ReportView report) {
        ReportTargetType targetType = ReportTargetType.valueOf(report.target().name());
        return new ReportCreateResponse(report.id().value(), targetType, report.targetId(), ReportStatus.PENDING);
    }

    private static ReportResponse toReportResponse(ReportListItem report) {
        return new ReportResponse(
                report.id().value(),
                report.reporterId().value(),
                ReportTargetType.valueOf(report.target().name()),
                report.targetId(),
                ReportStatus.valueOf(report.state().name()),
                report.reason(),
                report.titleSnapshot(),
                report.contentSnapshot(),
                report.createdAt(),
                report.action() == null
                        ? null
                        : ReportResolutionAction.valueOf(report.action().name()),
                report.resolutionReason(),
                report.processorId() == null ? null : report.processorId().value(),
                report.resolvedAt());
    }

    private static ReportResolveResponse toResolveResponse(ReportResolutionView report) {
        return new ReportResolveResponse(
                report.id().value(),
                ReportStatus.valueOf(report.state().name()),
                ReportResolutionAction.valueOf(report.action().name()),
                report.processorId().value(),
                report.resolvedAt(),
                report.targetAvailable(),
                report.targetHidden());
    }
}
