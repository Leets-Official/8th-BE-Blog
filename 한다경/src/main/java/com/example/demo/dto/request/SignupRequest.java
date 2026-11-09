package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Schema(description = "가입 이메일. 최대 254자이며 소문자로 저장합니다.", example = "leets@example.com")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "가입 비밀번호. 공백만 사용할 수 없으며 8~128자입니다.",
                format = "password", example = "ExamplePassword123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 128) String password,
        @Schema(description = "회원 닉네임. 최대 50자이며 앞뒤 공백을 제거합니다.", example = "리츠회원")
        @NotBlank @Size(max = 50) String nickname
) {}
