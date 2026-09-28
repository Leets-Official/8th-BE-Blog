package com.example.demo.global.healthcheck.controller;

import com.example.demo.global.healthcheck.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(healthCheckService.check());
    }
}
