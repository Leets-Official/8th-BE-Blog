package com.leets.mission.controller;

import com.leets.mission.dto.report.ReportCreateRequest;
import com.leets.mission.dto.report.ReportResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.global.exception.ErrorResponse;
import com.leets.mission.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고", description = "게시글 또는 댓글 신고 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "콘텐츠 신고",
            description =  "게시글 또는 댓글 중 하나를 신고합니다."
    )
    @PostMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "신고 접수 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReportResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 오류 또는 본인 콘텐츠 신고 (INVALID_INPUT_VALUE, SELF_REPORT_NOT_ALLOWED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요함 (UNAUTHORIZED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "신고할 게시글 또는 댓글을 찾을 수 없음 (POST_NOT_FOUND, COMMENT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 신고한 대상임 (DUPLICATE_REPORT)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportCreateRequest request,
            @Parameter(description = "요청 회원 ID", example = "1", required = true)
            @RequestHeader(value = "X-Member-Id") Long memberId
    ){
        ReportResponse response = reportService.createReport(request, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}