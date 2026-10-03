package com.example.demo.healthcheck.controller;

import com.example.demo.healthcheck.dto.HealthCheckDto;
import com.example.demo.healthcheck.service.HealthCheckService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Getter
@RequiredArgsConstructor
@RestController
public class HealthCheckController {

    final private HealthCheckService healthCheckService;

    @GetMapping("/health")
    public String isWorking() {
        return "Server is working!";
    }

    @PostMapping("/string/repeat")
    public HealthCheckDto.RepeatStringResponse repeat(@RequestBody HealthCheckDto.RepeatStringRequest text){
        return healthCheckService.repeatString(text.text());
    }
}
