package com.example.demo.controller;

import com.example.demo.dto.RepeatStringRequest;
import com.example.demo.dto.RepeatStringResponse;
import com.example.demo.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assignment", description = "1주차 과제용 API (공통 응답 형식을 쓰지 않음)")
@RestController
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @Operation(summary = "서버 상태 확인", description = "서버가 살아 있으면 문자열 ok 를 반환합니다.")
    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @Operation(summary = "문자열 반복", description = "받은 value 를 string_one, string_two 에 그대로 담아 반환합니다.")
    @PostMapping("/string/repeat")
    public RepeatStringResponse repeat(@RequestBody RepeatStringRequest request) {
        return assignmentService.repeat(request.value());
    }

}
