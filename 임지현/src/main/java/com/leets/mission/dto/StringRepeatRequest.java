package com.leets.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@Schema(description = "문자열 반복 요청")
public class StringRepeatRequest {
    @Schema(description = "반복할 문자열", example = "hello")
    private String value;
}
