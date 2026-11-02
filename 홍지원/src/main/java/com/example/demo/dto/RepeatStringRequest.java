package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "문자열 반복 요청")
public class RepeatStringRequest {

    @Schema(description = "반복할 문자열", example = "hello", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;
    
}
