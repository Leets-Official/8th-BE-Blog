package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문자열 반복 응답")
public record RepeatStringResponse(
        @Schema(description = "요청 문자열 (첫 번째)", example = "hello")
        @JsonProperty("string_one") String stringOne,
        @Schema(description = "요청 문자열 (두 번째)", example = "hello")
        @JsonProperty("string_two") String stringTwo
) {}
