package com.leets.choyeji.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(
        @NotBlank(message = "Refresh Token을 입력해주세요.")
        @Size(max = 512, message = "Refresh Token이 너무 깁니다.") String refreshToken
) {
}
