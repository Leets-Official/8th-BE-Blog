package com.leets.blog.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record ReissueRequest(
        @Schema(description = "로그인 또는 재발급 때 받은 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "Refresh Token은 필수 입력 항목입니다.")
        String refreshToken
) {
}
