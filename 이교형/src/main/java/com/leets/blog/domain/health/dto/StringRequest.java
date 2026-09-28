package com.leets.blog.domain.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "문자열 요청 (Health Check)")
public class StringRequest {

    @Schema(description = "요청 문자열 값", example = "ping")
    private String value;
}