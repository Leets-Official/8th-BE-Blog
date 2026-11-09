package com.example.demo.controller;

import com.example.demo.domain.report.ReportStatus;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ModerationDto;
import com.example.demo.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;

@RestController
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "신고 및 게시글 관리", description = "신고 접수·처리 및 게시글 상태 관리 API")
public class ModerationController {
    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping("/posts/{post_id}/reports")
    @Operation(summary = "게시글 신고")
    public ResponseEntity<ApiResponse<ModerationDto.ReportView>> reportPost(
            @Parameter(description = "신고할 게시글 ID", example = "1") @PathVariable("post_id") long postId,
            @Parameter(hidden = true) Principal principal,
            @RequestBody ModerationDto.ReportRequest request) {
        // 기존 Service 구조를 유지하며 인증된 회원 ID만 전달합니다.
        return ResponseEntity.status(201).body(ApiResponse.success("201", moderationService.reportPost(postId, principal.getName(), request)));
    }

    @PostMapping("/comments/{comment_id}/reports")
    @Operation(summary = "댓글 신고")
    public ResponseEntity<ApiResponse<ModerationDto.ReportView>> reportComment(
            @Parameter(description = "신고할 댓글 ID", example = "1") @PathVariable("comment_id") long commentId,
            @Parameter(hidden = true) Principal principal,
            @RequestBody ModerationDto.ReportRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("201", moderationService.reportComment(commentId, principal.getName(), request)));
    }

    @GetMapping("/reports")
    @Operation(summary = "신고 목록 조회", description = "관리자(ADMIN) 전용입니다.")
    public ApiResponse<List<ModerationDto.ReportView>> listReports(
            @Parameter(description = "신고 처리 상태") @RequestParam ReportStatus status,
            @Parameter(hidden = true) Principal principal) {
        return ApiResponse.success("200", moderationService.listReports(status, principal.getName()));
    }

    @PatchMapping("/reports/{report_id}")
    @Operation(summary = "신고 처리", description = "관리자(ADMIN) 전용입니다.")
    public ApiResponse<ModerationDto.ReportView> resolveReport(
            @Parameter(description = "신고 ID", example = "1") @PathVariable("report_id") long reportId,
            @Parameter(hidden = true) Principal principal,
            @RequestBody ModerationDto.ResolveRequest request) {
        return ApiResponse.success("200", moderationService.resolveReport(reportId, principal.getName(), request));
    }

    @PatchMapping("/posts/{post_id}/visibility")
    @Operation(summary = "게시글 공개 상태 변경", description = "관리자(ADMIN) 전용입니다.")
    public ApiResponse<ModerationDto.State> changeVisibility(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("post_id") long postId,
            @Parameter(hidden = true) Principal principal,
            @RequestBody ModerationDto.VisibilityRequest request) {
        return ApiResponse.success("200", moderationService.changeVisibility(postId, principal.getName(), request));
    }

    @PutMapping("/posts/{post_id}/accepted-comment/{comment_id}")
    @Operation(summary = "채택 댓글 지정")
    public ApiResponse<ModerationDto.State> acceptComment(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("post_id") long postId,
            @Parameter(description = "채택할 댓글 ID", example = "1") @PathVariable("comment_id") long commentId,
            @Parameter(hidden = true) Principal principal) {
        return ApiResponse.success("200", moderationService.acceptComment(postId, commentId, principal.getName()));
    }

    @DeleteMapping("/posts/{post_id}/accepted-comment")
    @Operation(summary = "채택 댓글 해제")
    public ApiResponse<ModerationDto.State> unacceptComment(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("post_id") long postId,
            @Parameter(hidden = true) Principal principal) {
        return ApiResponse.success("200", moderationService.unacceptComment(postId, principal.getName()));
    }
}
