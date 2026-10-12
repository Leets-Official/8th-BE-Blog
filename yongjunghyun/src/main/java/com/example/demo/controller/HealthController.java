package com.example.demo.controller; // 본인 패키지 경로에 맞게 자동 작성됨

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Server is running!";
    }
}