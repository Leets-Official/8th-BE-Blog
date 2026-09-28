package com.leets.blog.domain.health.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "문자열 응답 (Health Check)")
public class StringResponse {

    @Schema(description = "첫 번째 문자열 결과", example = "pong")
    @JsonProperty("string_one")
    private String stringOne;

    @Schema(description = "두 번째 문자열 결과", example = "status ok")
    @JsonProperty("string_two")
    private String stringTwo;
}