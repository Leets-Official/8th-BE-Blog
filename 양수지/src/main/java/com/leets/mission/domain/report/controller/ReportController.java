package com.leets.mission.domain.report.controller;

import com.leets.mission.domain.report.dto.ReportRequestDTO;
import com.leets.mission.domain.report.dto.ReportResponseDTO;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.domain.report.service.ReportService;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.ErrorResponseSchema;
import com.leets.mission.global.apiPayload.code.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "신고", description = "신고 API")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 게시글 신고
    @Operation(summary = "게시글 신고")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "신고 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류 / REPORT_400_1: 본인 글 신고 불가",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음 / USER_404_1: 사용자 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "REPORT_409_1: 이미 신고한 대상",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponseDTO.DetailDTO> reportPost(
            @PathVariable Long postId,
            @RequestBody @Valid ReportRequestDTO.CreateDTO request
    ) {
        return ApiResponse.of(SuccessStatus.REPORT_CREATED, reportService.report(ReportTargetType.POST, postId, request));
    }

    // 댓글 신고
    @Operation(summary = "댓글 신고")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "신고 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류 / REPORT_400_1: 본인 글 신고 불가",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "COMMENT_404_1: 댓글 없음 / USER_404_1: 사용자 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "REPORT_409_1: 이미 신고한 대상",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PostMapping("/comments/{commentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponseDTO.DetailDTO> reportComment(
            @PathVariable Long commentId,
            @RequestBody @Valid ReportRequestDTO.CreateDTO request
    ) {
        return ApiResponse.of(SuccessStatus.REPORT_CREATED, reportService.report(ReportTargetType.COMMENT, commentId, request));
    }

    // 신고 처리 (RESOLVED / REJECTED)
    @Operation(summary = "신고 처리")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "처리 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류 / REPORT_400_2: 처리 결과는 RESOLVED 또는 REJECTED만 가능",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "REPORT_404_1: 신고 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "REPORT_409_2: 이미 처리된 신고",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PatchMapping("/reports/{reportId}")
    public ApiResponse<ReportResponseDTO.DetailDTO> processReport(
            @PathVariable Long reportId,
            @RequestBody @Valid ReportRequestDTO.ProcessDTO request
    ) {
        return ApiResponse.of(SuccessStatus.REPORT_PROCESSED, reportService.processReport(reportId, request));
    }
}
