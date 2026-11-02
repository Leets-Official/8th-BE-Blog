package com.example.demo.controller;

import com.example.demo.dto.RepeatStringRequest;
import com.example.demo.dto.RepeatStringResponse;
import com.example.demo.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "과제 API", description = "기본 상태 확인 및 문자열 반복 API")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/health")
    @Operation(summary = "서버 상태 확인", description = "서버 상태를 문자열로 반환합니다.")
    public String health() {
        return assignmentService.healthCheck();
    }

    @PostMapping("/string/repeat")
    @Operation(summary = "문자열 반복", description = "입력 문자열을 두 응답 필드로 반환합니다.")
    public RepeatStringResponse repeatString(
            @RequestBody RepeatStringRequest request) {

        return assignmentService.repeatString(request.getValue());
    }
}
