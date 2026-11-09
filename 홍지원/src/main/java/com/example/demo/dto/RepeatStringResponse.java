package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Schema(description = "문자열 반복 응답")
public class RepeatStringResponse {

    @Schema(description = "첫 번째 문자열", example = "hello")
    @JsonProperty("string_one")
    private String stringOne;

    @Schema(description = "두 번째 문자열", example = "hello")
    @JsonProperty("string_two")
    private String stringTwo;

    public RepeatStringResponse(String stringOne, String stringTwo) {
        this.stringOne = stringOne;
        this.stringTwo = stringTwo;
    }
}
