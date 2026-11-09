package com.example.demo.auth.dto;

public class AuthResDto {

    public record SignupResponse(
            Long memberId
    ) {}

    public record TokenResult(
            String accessToken,
            String refreshToken
    ) {}

    public record LoginResponse(
            String accessToken
    ) {}
}