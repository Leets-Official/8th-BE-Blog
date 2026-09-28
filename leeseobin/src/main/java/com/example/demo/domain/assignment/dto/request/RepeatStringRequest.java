package com.example.demo.domain.assignment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RepeatStringRequest(
    @NotBlank(message = "반복할 문자열은 비어 있을 수 없습니다.")
    String text
) {
}
