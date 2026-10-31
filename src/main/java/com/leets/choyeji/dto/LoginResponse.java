package com.leets.choyeji.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
