package com.example.demo.controller.docs;

import com.example.demo.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
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
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 대상 ID·신고 사유 검증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "본인 게시글 신고 또는 접근할 수 없는 게시글",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원 또는 게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "중복 신고 또는 동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ReportResponse reportPost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        ReportCreateRequest request
    );

    @Operation(summary = "댓글 신고", description = "댓글에 대한 신고 사유를 접수하고 신고 정보를 반환합니다. 본인 댓글 신고와 중복 신고는 허용하지 않습니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 대상 ID·신고 사유 검증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "본인 댓글 신고 또는 댓글·소속 게시글에 접근할 수 없는 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원·댓글·소속 게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "중복 신고 또는 동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ReportResponse reportComment(
        @Parameter(description = "댓글 ID (양수)", example = "1")
        Long commentId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        ReportCreateRequest request
    );

    @Operation(summary = "신고 승인", description = "대기 중인 신고를 승인하고 대상 게시글 또는 댓글을 숨김 처리합니다. 이미 승인된 신고는 기존 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 신고 ID가 양수가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원이 없거나 삭제되었거나 신고가 없는 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "승인할 수 없는 신고 상태 또는 동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ReportResponse approve(
        @Parameter(description = "신고 ID (양수)", example = "1")
        Long reportId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );
}
