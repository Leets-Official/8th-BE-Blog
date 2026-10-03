package com.leets_8th_be.kanghyeonwoo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatStringRequest {

    @NotBlank(message = "문자열 값은 비어있거나 공백일 수 없습니다.")
    private String value;
}
