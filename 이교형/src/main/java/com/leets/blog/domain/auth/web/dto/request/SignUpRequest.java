package com.leets.blog.domain.auth.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 검증 message에는 응답할 ErrorCode 이름을 적음 (GlobalExceptionHandler가 변환)
@Schema(description = "회원가입 요청")
public record SignUpRequest(

        @Schema(description = "이메일 (50자 이하, 중복 불가)", example = "hong@test.com")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Email(message = "INVALID_EMAIL_FORMAT")
        @Size(max = 50, message = "INVALID_EMAIL_FORMAT")
        String email,

        @Schema(description = "비밀번호 (8~64자, 한글은 24자까지)", example = "test1234!")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(min = 8, max = 64, message = "INVALID_PASSWORD_FORMAT")
        String password,

        @Schema(description = "닉네임 (2~20자, 중복 불가)", example = "길동이")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(min = 2, max = 20, message = "INVALID_NICKNAME")
        String nickname
) {
}
