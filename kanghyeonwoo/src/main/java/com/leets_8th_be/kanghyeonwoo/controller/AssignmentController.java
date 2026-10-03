package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.dto.RepeatStringRequest;
import com.leets_8th_be.kanghyeonwoo.dto.RepeatStringResponse;
import com.leets_8th_be.kanghyeonwoo.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping({"/health", "/api/v1/health"})
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success(assignmentService.getHealthStatus());
    }

    @PostMapping({"/string/repeat", "/api/v1/string/repeat"})
    public ApiResponse<RepeatStringResponse> repeatString(@Valid @RequestBody RepeatStringRequest request) {
        RepeatStringResponse response = assignmentService.repeatString(request);
        return ApiResponse.success("문자열 복제에 성공하였습니다.", response);
    }
}
