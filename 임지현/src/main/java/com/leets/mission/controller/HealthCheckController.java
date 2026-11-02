package com.leets.mission.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "헬스체크", description = "서버 상태 확인 API")
@RestController
public class HealthCheckController {

    @Operation(summary = "서버 상태 확인", description = "서버가 요청을 처리할 수 있는 상태인지 확인합니다.")
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
