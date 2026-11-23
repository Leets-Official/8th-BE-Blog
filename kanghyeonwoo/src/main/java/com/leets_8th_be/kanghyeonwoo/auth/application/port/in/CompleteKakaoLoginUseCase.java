package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

public interface CompleteKakaoLoginUseCase {
    String complete(String state, String browser, String authorizationCode);
}
