package com.leets.mission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "문자열 반복 응답")
public class StringRepeatResponse {
    @Schema(description = "입력 문자열", example = "hello")
    @JsonProperty("string_one")
    private String stringOne;

    @Schema(description = "입력 문자열을 한 번 더 반복한 값", example = "hello")
    @JsonProperty("string_two")
    private String stringTwo;
}
