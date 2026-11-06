package com.leets.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RepeatStringResponse(
        @JsonProperty("string_one") @Schema(description = "입력한 문자열", example = "hello") String stringOne,
        @JsonProperty("string_two") @Schema(description = "입력한 문자열", example = "hello") String stringTwo
) {
}
