package com.example.demo.controller;

import com.example.demo.domain.report.ReportStatus;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ModerationDto;
import com.example.demo.service.ModerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ModerationController {
    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping("/posts/{post_id}/reports")
    public ResponseEntity<ApiResponse<ModerationDto.ReportView>> reportPost(
            @PathVariable("post_id") long postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ModerationDto.ReportRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("201", moderationService.reportPost(postId, userId, request)));
    }

    @PostMapping("/comments/{comment_id}/reports")
    public ResponseEntity<ApiResponse<ModerationDto.ReportView>> reportComment(
            @PathVariable("comment_id") long commentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ModerationDto.ReportRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("201", moderationService.reportComment(commentId, userId, request)));
    }

    @GetMapping("/reports")
    public ApiResponse<List<ModerationDto.ReportView>> listReports(
            @RequestParam ReportStatus status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success("200", moderationService.listReports(status, userId));
    }

    @PatchMapping("/reports/{report_id}")
    public ApiResponse<ModerationDto.ReportView> resolveReport(
            @PathVariable("report_id") long reportId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ModerationDto.ResolveRequest request) {
        return ApiResponse.success("200", moderationService.resolveReport(reportId, userId, request));
    }

    @PatchMapping("/posts/{post_id}/visibility")
    public ApiResponse<ModerationDto.State> changeVisibility(
            @PathVariable("post_id") long postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ModerationDto.VisibilityRequest request) {
        return ApiResponse.success("200", moderationService.changeVisibility(postId, userId, request));
    }

    @PutMapping("/posts/{post_id}/accepted-comment/{comment_id}")
    public ApiResponse<ModerationDto.State> acceptComment(
            @PathVariable("post_id") long postId,
            @PathVariable("comment_id") long commentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success("200", moderationService.acceptComment(postId, commentId, userId));
    }

    @DeleteMapping("/posts/{post_id}/accepted-comment")
    public ApiResponse<ModerationDto.State> unacceptComment(
            @PathVariable("post_id") long postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success("200", moderationService.unacceptComment(postId, userId));
    }
}
