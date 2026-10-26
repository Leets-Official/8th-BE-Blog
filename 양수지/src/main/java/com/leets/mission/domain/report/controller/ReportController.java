package com.leets.mission.domain.report.controller;

import com.leets.mission.domain.report.dto.ReportRequestDTO;
import com.leets.mission.domain.report.dto.ReportResponseDTO;
import com.leets.mission.domain.report.service.ReportService;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.code.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 게시글 신고
    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponseDTO.DetailDTO> reportPost(
            @PathVariable Long postId,
            @RequestBody @Valid ReportRequestDTO.CreateDTO request
    ) {
        return ApiResponse.of(SuccessStatus.REPORT_CREATED, reportService.reportPost(postId, request));
    }
}
