package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.*;
import com.leets_8th_be.kanghyeonwoo.service.ReportService;
import com.leets_8th_be.kanghyeonwoo.entity.ReportStatus;
import com.leets_8th_be.kanghyeonwoo.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Report", description = "신고 접수·조회·처리 API (과제용 사용자 ID 방식)")
public class ReportController {
    private final ReportService reports;

    @PostMapping("/posts/{postId}/reports")
    @Operation(summary = "게시글 신고", description = "본인·숨김 게시글은 신고할 수 없으며 동일 사용자의 중복 신고는 409입니다.")
    public ResponseEntity<ApiResponse<ReportCreateResponse>> reportPost(
            @PathVariable("postId") Long postId, @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "S201", "게시글 신고가 접수되었습니다.", reports.reportPost(postId, request)));
    }
    @PostMapping("/comments/{commentId}/reports")
    @Operation(summary = "댓글 신고", description = "본인·숨김·삭제 댓글과 숨김 게시글의 댓글은 신고할 수 없습니다. 중복 신고는 409입니다.")
    public ResponseEntity<ApiResponse<ReportCreateResponse>> reportComment(
            @PathVariable("commentId") Long commentId, @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "S201", "댓글 신고가 접수되었습니다.", reports.reportComment(commentId, request)));
    }

    @GetMapping("/reports")
    @Operation(summary = "신고 목록·스냅샷 검색", description = "필터는 AND, 검색어는 사유·제목/본문 스냅샷을 OR로 검색합니다. 관리자 제한은 없습니다.")
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse>>> getReports(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "status", required = false) ReportStatus status,
            @RequestParam(name = "targetType", required = false) ReportTargetType targetType,
            @RequestParam(name = "targetId", required = false) Long targetId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("신고 조회가 완료되었습니다.",
                reports.getReports(page, size, status, targetType, targetId, keyword)));
    }

    @PatchMapping("/reports/{reportId}/resolve")
    @Operation(summary = "신고 처리", description = "존재하는 사용자라면 처리할 수 있습니다. HIDE는 대상 숨김, KEEP는 현재 공개 상태 유지입니다. 재처리는 409입니다.")
    public ResponseEntity<ApiResponse<ReportResolveResponse>> resolve(
            @PathVariable("reportId") Long reportId, @Valid @RequestBody ReportResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("신고 처리가 완료되었습니다.", reports.resolve(reportId, request)));
    }

}
