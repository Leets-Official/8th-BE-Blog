package com.leets.choyeji.controller;

import com.leets.choyeji.auth.JwtMemberResolver;
import com.leets.choyeji.dto.AdminReportListResponse;
import com.leets.choyeji.dto.AdminReportResponse;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.dto.PostResponse;
import com.leets.choyeji.dto.ReportResolveRequest;
import com.leets.choyeji.entity.ReportStatus;
import com.leets.choyeji.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;
    private final JwtMemberResolver jwtMemberResolver;

    @Operation(summary = "관리자 신고 목록 조회")
    @GetMapping("/reports")
    public ApiResponse<AdminReportListResponse> listReports(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminService.listReports(jwtMemberResolver.resolve(authorization), status, page, size));
    }

    @Operation(summary = "관리자 신고 처리")
    @PatchMapping("/reports/{id}/resolve")
    public ApiResponse<AdminReportResponse> resolveReport(@PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ReportResolveRequest request) {
        return ApiResponse.success(adminService.resolveReport(jwtMemberResolver.resolve(authorization), id, request));
    }

    @Operation(summary = "관리자 게시글 숨김 해제")
    @PatchMapping("/posts/{postId}/restore")
    public ApiResponse<PostResponse> restorePost(@PathVariable Long postId,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(adminService.restorePost(jwtMemberResolver.resolve(authorization), postId));
    }
}
