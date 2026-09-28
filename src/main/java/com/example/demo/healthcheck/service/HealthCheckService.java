package com.example.demo.healthcheck.service;

import com.example.demo.healthcheck.converter.HealthCheckConverter;
import com.example.demo.healthcheck.dto.HealthCheckDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthCheckService {

    final private HealthCheckConverter healthCheckConverter;

    public HealthCheckDto.RepeatStringResponse repeatString(String text){
        return healthCheckConverter.toResponse(text);
    }
}
