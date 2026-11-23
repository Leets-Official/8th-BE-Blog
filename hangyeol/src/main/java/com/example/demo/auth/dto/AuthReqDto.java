package com.example.demo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthReqDto {

    @Schema(description = "회원가입 요청")
    public record SignupRequest(
            @Schema(description = "이메일", example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                   message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @Schema(description = "비밀번호 (영문+숫자, 8~64자)", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "비밀번호를 입력해주세요.")
            @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                     message = "비밀번호는 영문과 숫자를 모두 포함해야 합니다.")
            String password,

            @Schema(description = "닉네임 (한글/영문/숫자, 20자 이하)", example = "테스터", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "닉네임을 입력해주세요.")
            @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
            @Pattern(regexp = "^[가-힣A-Za-z0-9]+$",
                     message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            String nickname
    ) {}

    @Schema(description = "로그인 요청")
    public record LoginRequest(
            @Schema(description = "이메일", example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "이메일을 입력해주세요.")
            String email,

            @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "비밀번호를 입력해주세요.")
            String password
    ) {}
}
