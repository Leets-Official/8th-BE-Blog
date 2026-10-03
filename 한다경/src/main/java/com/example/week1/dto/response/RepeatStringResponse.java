package com.example.week1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RepeatStringResponse {

    @JsonProperty("string_one")
    private String stringOne;
    @JsonProperty("string_two")
    private String stringTwo;

    public RepeatStringResponse(String stringOne, String stringTwo) {
        this.stringOne = stringOne;
        this.stringTwo = stringTwo;
    }

    public String getStringOne() {
        return stringOne;
    }

    public String getStringTwo() {
        return stringTwo;
    }
}
