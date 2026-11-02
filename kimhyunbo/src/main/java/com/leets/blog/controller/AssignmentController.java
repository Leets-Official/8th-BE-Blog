package com.leets.blog.controller;

import com.leets.blog.dto.RepeatStringRequest;
import com.leets.blog.dto.RepeatStringResponse;
import com.leets.blog.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "1주차 과제", description = "헬스체크와 문자열 반환 API")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "헬스체크", description = "서버가 실행 중인지 확인합니다. 정상이면 {\"status\": \"OK\"}를 반환합니다.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK");
    }

    @Operation(summary = "문자열 2개 반환", description = "요청으로 받은 문자열을 string_one, string_two에 그대로 담아 반환합니다.")
    @PostMapping("/string/repeat")
    public RepeatStringResponse repeat(@RequestBody RepeatStringRequest request) {
        return assignmentService.repeat(request.value());
    }
}
