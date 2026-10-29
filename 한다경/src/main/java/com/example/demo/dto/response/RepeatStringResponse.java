package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepeatStringResponse {

    @JsonProperty("string_one")
    @Schema(description = "입력 문자열의 첫 번째 반환값", example = "Leets")
    private final String stringOne;

    @JsonProperty("string_two")
    @Schema(description = "입력 문자열의 두 번째 반환값", example = "Leets")
    private final String stringTwo;
}
