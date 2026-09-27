package org.example._thbe.domain.StringRepeat.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StringRepeatResponse {
    private String string_one;
    private String string_two;

    public static StringRepeatResponse of(String string_one,String string_two) {
        return new StringRepeatResponse(string_one,string_two);
    }
}
