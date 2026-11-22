package com.leets.blog.domain.auth.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(

        @Schema(description = "이메일", example = "hong@test.com")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        String email,

        @Schema(description = "비밀번호", example = "test1234!")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        String password
) {
}
