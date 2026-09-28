package com.example.demo.domain.assignment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RepeatStringResponse(
    @JsonProperty("string_one")
    String stringOne,

    @JsonProperty("string_two")
    String stringTwo
) {
    public static RepeatStringResponse of(String value) {
        return new RepeatStringResponse(value, value);
    }
}
