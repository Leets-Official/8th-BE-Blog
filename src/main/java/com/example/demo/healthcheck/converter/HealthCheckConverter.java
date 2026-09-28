package com.example.demo.healthcheck.converter;

import com.example.demo.healthcheck.dto.HealthCheckDto;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckConverter {
    public HealthCheckDto.RepeatStringResponse toResponse(String text){
        return new HealthCheckDto.RepeatStringResponse(text,text);
    }
}
