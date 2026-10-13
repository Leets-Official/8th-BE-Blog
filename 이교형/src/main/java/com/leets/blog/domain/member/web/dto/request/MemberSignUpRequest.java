package com.leets.blog.domain.member.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 검증 message에는 응답할 ErrorCode 이름을 적음 (GlobalExceptionHandler가 변환)
@Schema(description = "회원가입 요청")
public record MemberSignUpRequest(

        @Schema(description = "이름 (공백 불가, 50자 이하)", example = "홍길동")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(max = 50, message = "INVALID_MEMBER_INPUT")
        String name,

        @Schema(description = "이메일 (공백 불가, 50자 이하, 중복 불가)", example = "hong@test.com")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Email(message = "INVALID_EMAIL_FORMAT")
        @Size(max = 50, message = "INVALID_MEMBER_INPUT")
        String email,

        @Schema(description = "비밀번호 (8~64자)", example = "test1234!")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(min = 8, max = 64, message = "INVALID_MEMBER_INPUT")
        String password,

        @Schema(description = "나이 (선택, 0~127)", example = "25")
        @Min(value = 0, message = "INVALID_MEMBER_INPUT")
        @Max(value = 127, message = "INVALID_MEMBER_INPUT")
        Integer age
) {
}
