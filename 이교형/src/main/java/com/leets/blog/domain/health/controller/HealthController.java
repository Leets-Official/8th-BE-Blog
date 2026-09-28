package com.leets.blog.domain.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check API", description = "서버 정상 작동 확인용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "서버 정상 작동 확인", description = "서버가 정상 작동하는지 확인합니다.")
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
