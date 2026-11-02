package com.leets.mission.controller;

import com.leets.mission.dto.RepeatStringRequest;
import com.leets.mission.dto.RepeatStringResponse;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.ErrorResponseSchema;
import com.leets.mission.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "과제", description = "과제 API")
@RestController
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @Operation(summary = "서버 상태 확인")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "정상")
    @GetMapping("/health")
    public String health(){
        return "ok";
    }

    @Operation(summary = "문자열 반복")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "반복 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 요청 본문 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PostMapping("/string/repeat")
    public ApiResponse<RepeatStringResponse> repeat(@RequestBody RepeatStringRequest request){
        return ApiResponse.onSuccess(assignmentService.repeat(request.value()));
    }
}
