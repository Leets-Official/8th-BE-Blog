package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RepeatStringResponse {

    @JsonProperty("string_one")
    private String stringOne;

    @JsonProperty("string_two")
    private String stringTwo;

    public RepeatStringResponse(String stringOne, String stringTwo) {
        this.stringOne = stringOne;
        this.stringTwo = stringTwo;
    }
}