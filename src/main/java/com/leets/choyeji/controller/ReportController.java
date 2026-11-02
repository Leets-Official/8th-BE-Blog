package com.leets.choyeji.controller;

import com.leets.choyeji.auth.JwtMemberResolver;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.dto.ReportRequest;
import com.leets.choyeji.dto.ReportResponse;
import com.leets.choyeji.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final JwtMemberResolver jwtMemberResolver;

    @Operation(summary = "게시글 신고")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<ReportResponse> create(@PathVariable Long postId,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ReportRequest request) {
        return ApiResponse.success(reportService.create(jwtMemberResolver.resolve(authorization), postId, request));
    }
}
