package com.example.demo.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.example.demo.dto.request.ReportCreateRequest;
import com.example.demo.dto.response.ReportResponse;

@Tag(name = "신고", description = "게시글 및 댓글 신고와 승인")
public interface ReportControllerDocs {

    @Operation(summary = "게시글 신고", description = "게시글에 대한 신고 사유를 접수하고 신고 정보를 반환합니다. 본인 게시글 신고와 중복 신고는 허용하지 않습니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    ReportResponse reportPost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        ReportCreateRequest request
    );

    @Operation(summary = "댓글 신고", description = "댓글에 대한 신고 사유를 접수하고 신고 정보를 반환합니다. 본인 댓글 신고와 중복 신고는 허용하지 않습니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    ReportResponse reportComment(
        @Parameter(description = "댓글 ID (양수)", example = "1")
        Long commentId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        ReportCreateRequest request
    );

    @Operation(summary = "신고 승인", description = "대기 중인 신고를 승인하고 대상 게시글 또는 댓글을 숨김 처리합니다. 이미 승인된 신고는 기존 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ReportResponse approve(
        @Parameter(description = "신고 ID (양수)", example = "1")
        Long reportId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );
}
