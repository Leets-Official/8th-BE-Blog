package com.leets.mission.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문자열 반복 응답")
public record RepeatStringResponse (
        @Schema(description = "첫 번째 문자열", example = "hello")
        @JsonProperty("string_one") String stringOne,

        @Schema(description = "두 번째 문자열", example = "hello")
        @JsonProperty("string_two") String stringTwo

){

}
