package com.leets.choyeji.dto;

public record LoginResponse(String accessToken, String refreshToken, String tokenType,
                            long expiresIn, long refreshExpiresIn) {
}
