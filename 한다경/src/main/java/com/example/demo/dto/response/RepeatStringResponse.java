package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepeatStringResponse {

    @JsonProperty("string_one")
    private final String stringOne;

    @JsonProperty("string_two")
    private final String stringTwo;
}
