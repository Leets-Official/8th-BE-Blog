package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

public interface LogoutUseCase {
    void logout(String refreshToken);
}
