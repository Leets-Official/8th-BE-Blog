package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.dto.RepeatStringRequest;
import com.leets_8th_be.kanghyeonwoo.dto.RepeatStringResponse;
import com.leets_8th_be.kanghyeonwoo.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assignment", description = "기본 과제 및 헬스체크 API")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "서버 헬스체크", description = "서버 정상 구동 상태를 확인합니다.")
    @GetMapping({"/health", "/api/v1/health"})
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getHealthStatus()));
    }

    @Operation(summary = "문자열 반복", description = "요청받은 문자열과 반복 횟수를 통해 결합된 문자열을 반환합니다.")
    @PostMapping({"/string/repeat", "/api/v1/string/repeat"})
    public ResponseEntity<ApiResponse<RepeatStringResponse>> repeatString(@Valid @RequestBody RepeatStringRequest request) {
        RepeatStringResponse response = assignmentService.repeatString(request);
        return ResponseEntity.ok(ApiResponse.success("문자열 복제에 성공하였습니다.", response));
    }
}
