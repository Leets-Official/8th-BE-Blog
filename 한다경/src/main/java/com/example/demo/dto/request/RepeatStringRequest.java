package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepeatStringRequest {

    @NotBlank(message = "value는 비어 있을 수 없습니다.")
    private @Schema(description = "반환할 문자열 (필수, 공백 불가)", example = "Leets")
 String value;
}
