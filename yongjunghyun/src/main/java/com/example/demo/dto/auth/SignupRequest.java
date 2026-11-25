package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// message에는 검증 실패 시 응답할 ErrorCode 이름을 적는다 (3주차와 같은 방식)
public record SignupRequest(
        @NotBlank(message = "MEMBER_EMAIL_EMPTY")
        @Email(message = "MEMBER_EMAIL_INVALID")
        String email,

        // 영문 1개 이상 + 숫자 1개 이상, 전체 8~20자
        @NotBlank(message = "MEMBER_PASSWORD_EMPTY")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "MEMBER_PASSWORD_INVALID")
        String password,

        @NotBlank(message = "MEMBER_NICKNAME_EMPTY")
        @Size(max = 20, message = "MEMBER_NICKNAME_TOO_LONG")
        String nickname,

        @NotBlank(message = "MEMBER_NAME_EMPTY")
        String name,

        @Pattern(regexp = "^01\\d{8,9}$", message = "MEMBER_TEL_INVALID")
        @NotBlank(message = "MEMBER_TEL_INVALID")
        String tel
) {
}
