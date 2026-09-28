package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RepeatStringResponse(
        @JsonProperty("string_one") String stringOne,
        @JsonProperty("string_two") String stringTwo
) {
}
