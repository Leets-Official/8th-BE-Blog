package com.leets.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문자열 반환 결과")
public record RepeatStringResponse(
        @Schema(description = "요청한 문자열", example = "hello")
        @JsonProperty("string_one") String stringOne,

        @Schema(description = "요청한 문자열", example = "hello")
        @JsonProperty("string_two") String stringTwo
) {
}
