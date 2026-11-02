package com.leets.mission.controller;

import com.leets.mission.dto.report.ReportCreateRequest;
import com.leets.mission.dto.report.ReportResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "신고", description = "게시글 또는 댓글 신고 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "콘텐츠 신고", description = "게시글 또는 댓글을 신고합니다. 신고 대상은 둘 중 하나만 지정해야 합니다.")
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportCreateRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        ReportResponse response = reportService.createReport(request, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}