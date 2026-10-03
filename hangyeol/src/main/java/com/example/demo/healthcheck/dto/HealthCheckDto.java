package com.example.demo.healthcheck.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthCheckDto {
    public record RepeatStringResponse (String firstText , String secondText){};

    public record RepeatStringRequest (String text){};
}
