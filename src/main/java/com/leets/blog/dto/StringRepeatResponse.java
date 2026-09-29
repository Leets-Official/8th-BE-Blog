package com.leets.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record StringRepeatResponse(
        @Schema(example = "hello") @JsonProperty("string_one") String stringOne,
        @Schema(example = "hello") @JsonProperty("string_two") String stringTwo
) {
}
