package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepeatStringRequest {

    @NotBlank(message = "value는 비어 있을 수 없습니다.")
    private String value;
}
