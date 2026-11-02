package com.leets.mission.controller;

import com.leets.mission.domain.report.ReportStatus;
import com.leets.mission.dto.report.ReportResponse;
import com.leets.mission.dto.report.ReportDecisionRequest;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.global.exception.ErrorResponse;
import com.leets.mission.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "관리자 신고", description = "관리자의 신고 목록 조회 및 신고 처리 API")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @Operation(
            summary = "신고 목록 조회",
            description = "관리자가 신고 상태별 목록을 페이지 단위로 조회합니다."
    )
    @GetMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "페이지 번호, 페이지 크기 또는 상태값 오류 (INVALID_PAGE_NUMBER, INVALID_PAGE_SIZE, INVALID_INPUT_VALUE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요함 (UNAUTHORIZED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "운영자 권한이 없음 (ADMIN_ACCESS_DENIED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Page<ReportResponse>> getReports(
            @Parameter(
                    description = "조회할 신고 상태",
                    example = "PENDING"
            )
            @RequestParam(defaultValue = "PENDING") ReportStatus status,

            @Parameter(description = "페이지 번호, 1부터 시작", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기 (1~50)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "요청 회원 ID", example = "1", required = true)
            @RequestHeader("X-Member-Id") Long memberId
    ){
        if (page < 1) {
            throw new CustomException(ErrorCode.INVALID_PAGE_NUMBER);
        }

        if (size < 1 || size > 50) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                reportService.getReports(memberId, status, pageable)
        );
    }

    @Operation(
            summary = "신고 처리",
            description = "관리자가 신고 상태를 승인(RESOLVED) 또는 기각(DISMISSED)으로 변경합니다."
    )
    @PatchMapping("/{reportId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 처리 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReportResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 또는 신고 ID 형식 오류 (INVALID_INPUT_VALUE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요함 (UNAUTHORIZED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "운영자 권한이 없음 (ADMIN_ACCESS_DENIED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "신고 또는 신고 대상 콘텐츠를 찾을 수 없음 (REPORT_NOT_FOUND, POST_NOT_FOUND, COMMENT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 처리된 신고이거나 콘텐츠가 이미 숨김 처리됨 (REPORT_ALREADY_PROCESSED, POST_ALREADY_HIDDEN, COMMENT_ALREADY_HIDDEN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReportResponse> processReport(
            @Parameter(description = "처리할 신고 ID", example = "1")
            @PathVariable Long reportId,
            @Valid @RequestBody ReportDecisionRequest request,
            @Parameter(description = "관리자 회원 ID", example = "3", required = true)
            @RequestHeader("X-Member-Id") Long memberId
    ){

        return ResponseEntity.ok(
                reportService.processReport(reportId, request, memberId)
        );
    }
}