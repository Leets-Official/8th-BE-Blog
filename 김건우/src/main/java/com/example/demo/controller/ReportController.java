package com.example.demo.controller;

import com.example.demo.dto.ReportRequest;
import com.example.demo.dto.ReportResponse;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.ErrorResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.global.SwaggerErrorExamples;
import com.example.demo.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "신고 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 게시글 신고: "신고" 라는 자원을 게시글 하위에 생성 -> CRUD형
    // @ResponseStatus 는 문서용 (실제 상태 코드는 ApiResponse.success 가 결정)
    @Operation(summary = "게시글 신고",
            description = "게시글을 신고합니다. 신고는 PENDING 상태로 접수됩니다. "
                    + "같은 사용자가 같은 글을 다시 신고하면 409 이고, 본인 글과 숨김 처리된 글은 신고할 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 본인 글 신고",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "COMMON_400_1", summary = "입력값 검증 실패", value = SwaggerErrorExamples.INVALID_BODY),
                                    @ExampleObject(name = "REPORT_400_1", summary = "본인 글 신고", value = SwaggerErrorExamples.SELF_REPORT_NOT_ALLOWED)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "POST_404_1", summary = "게시글 없음 (숨김 포함)", value = SwaggerErrorExamples.POST_NOT_FOUND),
                                    @ExampleObject(name = "USER_404_1", summary = "사용자 없음", value = SwaggerErrorExamples.USER_NOT_FOUND)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 신고한 게시글",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "REPORT_409_1", value = SwaggerErrorExamples.ALREADY_REPORTED)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Parameter(description = "신고할 게시글 ID", example = "1") @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReportRequest request) {
        return ApiResponse.success(SuccessCode.REPORT_CREATED, reportService.createReport(postId, userId, request));
    }

    // 신고 처리 완료: 상태 전이 + 검증을 묶은 행위 -> 액션형
    @Operation(summary = "신고 처리 완료",
            description = "신고 상태를 PENDING 에서 RESOLVED 로 변경하고 처리 시각을 기록합니다. 이미 처리된 신고는 409 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "REPORT_404_1", value = SwaggerErrorExamples.REPORT_NOT_FOUND))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 처리 완료된 신고",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "REPORT_409_2", value = SwaggerErrorExamples.REPORT_ALREADY_RESOLVED)))
    })
    @PostMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId) {
        return ApiResponse.success(SuccessCode.REPORT_RESOLVED, reportService.resolveReport(reportId));
    }
}
