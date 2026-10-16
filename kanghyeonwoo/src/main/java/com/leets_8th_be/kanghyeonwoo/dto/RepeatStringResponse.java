package com.leets_8th_be.kanghyeonwoo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatStringResponse {

    @JsonProperty("string_one")
    private String stringOne;

    @JsonProperty("string_two")
    private String stringTwo;

    public static RepeatStringResponse of(String str1, String str2) {
        return new RepeatStringResponse(str1, str2);
    }
}
