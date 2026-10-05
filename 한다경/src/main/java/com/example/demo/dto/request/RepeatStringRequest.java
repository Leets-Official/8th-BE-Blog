package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RepeatStringRequest {

    @NotBlank(message = "value는 비어 있을 수 없습니다.")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
