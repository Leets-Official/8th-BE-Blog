package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {
    // 인증에 필요한 요청과 응답을 한 파일에 모으고 비밀번호는 응답에서 제외합니다.
    public record Signup(
            @NotBlank @Email @Size(max = 50) @Schema(example = "member@example.com") String email,
            @NotBlank @Size(min = 8, max = 72) @Schema(example = "password123!", description = "8자 이상, UTF-8 72바이트 이하") String password,
            @NotBlank @Size(max = 50) @Schema(example = "지원", description = "기존 회원 name에 저장되는 닉네임") String nickname) {}

    public record Login(
            @NotBlank @Email @Size(max = 50) @Schema(example = "member@example.com") String email,
            @NotBlank @Size(max = 72) @Schema(example = "password123!") String password) {}

    public record Refresh(@NotBlank @Size(max = 100) @Schema(description = "로그인 또는 직전 재발급 응답의 refreshToken") String refreshToken) {}

    public record MemberView(Long memberId, String email, String nickname) {}

    public record Tokens(String tokenType, String accessToken, String refreshToken,
                         @Schema(description = "Access Token 유효 시간(초)", example = "900") long expiresIn) {}
}
