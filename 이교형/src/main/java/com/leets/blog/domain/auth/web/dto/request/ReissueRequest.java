package com.leets.blog.domain.auth.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record ReissueRequest(

        @Schema(description = "로그인·재발급 때 받은 Refresh Token")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        String refreshToken
) {
}
