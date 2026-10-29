package com.leets_8th_be.kanghyeonwoo.report.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.*;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import com.leets_8th_be.kanghyeonwoo.shared.web.PageResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.GetReportsUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportSearchQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportListItem;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportResolutionView;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Report", description = "신고 접수·조회·처리 API (과제용 사용자 ID 방식)")
@RequestMapping(ApiPaths.API_V2)
public class ReportController {
    private final ReportPostUseCase reportPostUseCase;
    private final ReportCommentUseCase reportCommentUseCase;
    private final GetReportsUseCase getReportsUseCase;
    private final ResolveReportUseCase resolveReportUseCase;

    @PostMapping("/posts/{postId}/reports")
    @Operation(summary = "게시글 신고", description = "본인·숨김 게시글은 신고할 수 없으며 동일 사용자의 중복 신고는 409입니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 신고 접수 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "신고 요청 검증 실패",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 콘텐츠 신고 불가",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 신고자 없음·숨김 게시글",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 신고한 게시글",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", ref = "#/components/responses/MethodNotAllowed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public ResponseEntity<ApiResponse<ReportCreateResponse>> reportPost(
            @io.swagger.v3.oas.annotations.Parameter(description = "신고할 게시글 ID", example = "1")
            @PathVariable("postId") Long postId, @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(ApiResponseCode.RESOURCE_CREATED.getHttpStatus()).body(ApiResponse.success(
                ApiResponseCode.RESOURCE_CREATED, "게시글 신고가 접수되었습니다.",
                toCreateResponse(reportPostUseCase.report(new ReportPostCommand(new PostId(postId),
                        new UserId(request.reporterId()), request.reason())))));
    }
    @PostMapping("/comments/{commentId}/reports")
    @Operation(summary = "댓글 신고", description = "본인·숨김·삭제 댓글과 숨김 게시글의 댓글은 신고할 수 없습니다. 중복 신고는 409입니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "댓글 신고 접수 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "신고 요청 검증 실패",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 콘텐츠 신고 불가",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글 또는 신고자 없음·숨김 대상",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 신고 또는 삭제된 댓글",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", ref = "#/components/responses/MethodNotAllowed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public ResponseEntity<ApiResponse<ReportCreateResponse>> reportComment(
            @io.swagger.v3.oas.annotations.Parameter(description = "신고할 댓글 ID", example = "1")
            @PathVariable("commentId") Long commentId, @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(ApiResponseCode.RESOURCE_CREATED.getHttpStatus()).body(ApiResponse.success(
                ApiResponseCode.RESOURCE_CREATED, "댓글 신고가 접수되었습니다.",
                toCreateResponse(reportCommentUseCase.report(new ReportCommentCommand(new CommentId(commentId),
                        new UserId(request.reporterId()), request.reason())))));
    }

    @GetMapping("/reports")
    @Operation(summary = "신고 목록·스냅샷 검색", description = "필터는 AND, 검색어는 사유·제목/본문 스냅샷을 OR로 검색합니다. 관리자 제한은 없습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고 목록 조회 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "페이지 또는 검색 조건 형식 오류",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", ref = "#/components/responses/MethodNotAllowed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse>>> getReports(
            @io.swagger.v3.oas.annotations.Parameter(description = "1부터 시작하는 페이지 번호", schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "1", example = "1"))
            @RequestParam(name = "page", defaultValue = "1") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기 (최대 100)", schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "100", example = "10"))
            @RequestParam(name = "size", defaultValue = "10") int size,
            @io.swagger.v3.oas.annotations.Parameter(description = "신고 상태", example = "PENDING")
            @RequestParam(name = "status", required = false) ReportStatus status,
            @io.swagger.v3.oas.annotations.Parameter(description = "신고 대상 종류", example = "POST")
            @RequestParam(name = "targetType", required = false) ReportTargetType targetType,
            @io.swagger.v3.oas.annotations.Parameter(description = "대상 ID", example = "1")
            @RequestParam(name = "targetId", required = false) Long targetId,
            @io.swagger.v3.oas.annotations.Parameter(description = "사유·제목 스냅샷·본문 스냅샷 검색어", example = "욕설")
            @RequestParam(name = "keyword", required = false) String keyword) {
        ReportPage result = getReportsUseCase.getReports(new ReportSearchQuery(page, size,
                status == null ? null : ReportState.valueOf(status.name()),
                targetType == null ? null : ReportTarget.valueOf(targetType.name()), targetId, keyword));
        PageResponse<ReportResponse> response = new PageResponse<>(result.items().stream()
                .map(ReportController::toReportResponse).toList(), result.page(), result.size(),
                result.totalElements(), result.totalPages());
        return ResponseEntity.ok(ApiResponse.success("신고 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/reports/{reportId}/resolve")
    @Operation(summary = "신고 처리", description = "존재하는 사용자라면 처리할 수 있습니다. HIDE는 대상 숨김, KEEP는 현재 공개 상태 유지입니다. 재처리는 409입니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고 처리 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "처리 요청 검증 실패",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고 또는 처리자 없음",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 처리했거나 삭제된 대상",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", ref = "#/components/responses/MethodNotAllowed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public ResponseEntity<ApiResponse<ReportResolveResponse>> resolve(
            @io.swagger.v3.oas.annotations.Parameter(description = "처리할 신고 ID", example = "1")
            @PathVariable("reportId") Long reportId, @Valid @RequestBody ReportResolveRequest request) {
        ReportResolutionView result = resolveReportUseCase.resolve(new ResolveReportCommand(new ReportId(reportId),
                new UserId(request.processorId()), ReportAction.valueOf(request.action().name()),
                request.resolutionReason()));
        return ResponseEntity.ok(ApiResponse.success("신고 처리가 완료되었습니다.", toResolveResponse(result)));
    }

    private static ReportCreateResponse toCreateResponse(ReportView report) {
        ReportTargetType targetType = ReportTargetType.valueOf(report.target().name());
        return new ReportCreateResponse(report.id().value(), targetType, report.targetId(), ReportStatus.PENDING);
    }

    private static ReportResponse toReportResponse(ReportListItem report) {
        return new ReportResponse(report.id().value(), report.reporterId().value(),
                ReportTargetType.valueOf(report.target().name()), report.targetId(),
                ReportStatus.valueOf(report.state().name()), report.reason(), report.titleSnapshot(),
                report.contentSnapshot(), report.createdAt(), report.action() == null ? null
                : com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportResolutionAction.valueOf(report.action().name()),
                report.resolutionReason(), report.processorId() == null ? null : report.processorId().value(),
                report.resolvedAt());
    }

    private static ReportResolveResponse toResolveResponse(ReportResolutionView report) {
        return new ReportResolveResponse(report.id().value(), ReportStatus.valueOf(report.state().name()),
                com.leets_8th_be.kanghyeonwoo.report.adapter.in.web.dto.ReportResolutionAction.valueOf(report.action().name()),
                report.processorId().value(), report.resolvedAt(), report.targetAvailable(), report.targetHidden());
    }

}
