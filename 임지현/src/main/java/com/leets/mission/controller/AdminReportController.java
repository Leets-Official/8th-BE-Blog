package com.leets.mission.controller;

import com.leets.mission.domain.report.ReportStatus;
import com.leets.mission.dto.report.ReportResponse;
import com.leets.mission.dto.report.ReportDecisionRequest;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.service.ReportService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(defaultValue = "PENDING") ReportStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

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
    public ResponseEntity<ReportResponse> processReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportDecisionRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return ResponseEntity.ok(
                reportService.processReport(reportId, request, memberId)
        );
    }
}