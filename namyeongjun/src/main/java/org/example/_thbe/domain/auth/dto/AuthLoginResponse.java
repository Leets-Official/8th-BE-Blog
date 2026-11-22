package org.example._thbe.domain.auth.dto;

public record AuthLoginResponse(String accessToken,Long accessExp,String refreshToken,Long refreshExp) {
}
