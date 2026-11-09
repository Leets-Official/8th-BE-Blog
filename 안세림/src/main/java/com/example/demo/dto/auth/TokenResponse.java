package com.example.demo.dto.auth;

public record TokenResponse(String grantType, String accessToken, String refreshToken) {
}