package com.example.demo.healthcheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthCheckDto {
    @Schema(description = "문자열 반복 응답")
    public record RepeatStringResponse (
            @Schema(description = "첫 번째 문자열", example = "hello")
            String firstText ,
            @Schema(description = "두 번째 문자열", example = "hello")
            String secondText){};

    @Schema(description = "문자열 반복 요청")
    public record RepeatStringRequest (
            @Schema(description = "반복할 문자열", example = "hello")
            String text){};
}
