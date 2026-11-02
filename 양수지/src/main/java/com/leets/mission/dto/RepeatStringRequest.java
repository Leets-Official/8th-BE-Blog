package com.leets.mission.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문자열 반복 요청")
public record RepeatStringRequest(
        @Schema(description = "반복할 문자열", example = "hello")
        String value
) {

}


