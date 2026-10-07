package com.leets.mission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StringRepeatResponse {
    @JsonProperty("string_one")
    private String stringOne;
    @JsonProperty("string_two")
    private String stringTwo;
}
